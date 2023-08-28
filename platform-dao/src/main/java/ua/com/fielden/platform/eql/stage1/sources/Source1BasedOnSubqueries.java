package ua.com.fielden.platform.eql.stage1.sources;

import static java.lang.String.format;
import static java.util.Collections.emptySortedMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.eql.meta.EqlEntityMetadataGenerator.H_ENTITY;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.eql.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.eql.meta.EqlDomainMetadata;
import ua.com.fielden.platform.eql.meta.query.AbstractPropInfo;
import ua.com.fielden.platform.eql.meta.query.EntityTypePropInfo;
import ua.com.fielden.platform.eql.meta.query.PrimTypePropInfo;
import ua.com.fielden.platform.eql.meta.query.QuerySourceInfo;
import ua.com.fielden.platform.eql.stage1.TransformationContext1;
import ua.com.fielden.platform.eql.stage1.operands.queries.SourceQuery1;
import ua.com.fielden.platform.eql.stage2.operands.queries.SourceQuery2;
import ua.com.fielden.platform.eql.stage2.sources.Source2BasedOnSubqueries;

public class Source1BasedOnSubqueries extends AbstractSource1<Source2BasedOnSubqueries> {
    public static final String ERR_CONFLICT_BETWEEN_YIELDED_AND_DECLARED_PROP_TYPE = "There is a problem while trying to determine the type for property [%s] of a query source based on subqueries with result type [%s].\n"
            + "Declared type is [%s].\nActual yield type is [%s].";
    
    private final List<SourceQuery1> models = new ArrayList<>();
    private final boolean isSyntheticEntity;
    private final Class<? extends AbstractEntity<?>> sourceType;

    public Source1BasedOnSubqueries(final List<SourceQuery1> models, final String alias, final Integer id, final Class<? extends AbstractEntity<?>> syntheticEntityType) {
        super(alias, id);
        this.isSyntheticEntity = syntheticEntityType != null;
        this.sourceType = determineSourceType(models, syntheticEntityType);
        this.models.addAll(models);
    }

    private static Class<? extends AbstractEntity<?>> determineSourceType(final List<SourceQuery1> models, final Class<? extends AbstractEntity<?>> syntheticEntityType) {
        if (syntheticEntityType != null) {
            return syntheticEntityType;
        } else {
            final Set<Class<? extends AbstractEntity<?>>> modelsResultTypes = new HashSet<>();
            for (final SourceQuery1 model : models) {
                modelsResultTypes.add(model.resultType);
            }
            
            if (modelsResultTypes.size() != 1) {
                throw new EqlStage1ProcessingException("Models of query source have different result types " + modelsResultTypes + ". While making select(..) or join(..)/leftJoin(..) from multiple models it should be ensured that they are of the same result type.");
            }        

            return modelsResultTypes.iterator().next();    
        }
    }
    
    @Override
    public Source2BasedOnSubqueries transform(final TransformationContext1 context) {
        final List<SourceQuery2> transformedQueries = models.stream().map(m -> m.transform(context)).collect(toList());
        final QuerySourceInfo<?> ei = obtainQuerySourceInfo(context.domainInfo, transformedQueries, sourceType(), isSyntheticEntity);
        return new Source2BasedOnSubqueries(transformedQueries, alias, id, ei, isSyntheticEntity);
    }
    
    @Override
    public Class<? extends AbstractEntity<?>> sourceType() {
        return sourceType;
    }

    public static <T extends AbstractEntity<?>> QuerySourceInfo<T> produceQuerySourceInfoForEntityType(final EqlDomainMetadata domainInfo, final List<SourceQuery2> models, final Class<T> sourceType, final boolean isComprehensive) {
        final QuerySourceInfo<T> querySourceInfo = new QuerySourceInfo<>(sourceType, isComprehensive);
        final SortedMap<String, AbstractPropInfo<?>> declaredProps = EntityAggregates.class.equals(sourceType) ? emptySortedMap() : domainInfo.getQuerySourceInfo(sourceType).getProps();
        final Collection<YieldInfoNode> yieldInfoNodes = YieldInfoNodesGenerator.generate(models);
        for (final YieldInfoNode yield : yieldInfoNodes) {
            final AbstractPropInfo<?> declaredProp = declaredProps.get(yield.name);
            if (declaredProp != null) {
                // The only thing that has to be taken from declared is its structure (in case of UE or complex value)
                if (declaredProp instanceof EntityTypePropInfo<?>) { // TODO here we assume that yield is of ET (this will help to handle the case of yielding ID, which currently is just long only.
                    final EntityTypePropInfo<?> declaredEntityTypePropInfo = (EntityTypePropInfo<?>) declaredProp;
                    if (!(yield.propType == null || isEntityType(yield.propType.javaType()) && yield.propType.javaType().equals(declaredEntityTypePropInfo.javaType()) || Long.class.equals(yield.propType.javaType()))) {
                        throw new EqlStage1ProcessingException(format(ERR_CONFLICT_BETWEEN_YIELDED_AND_DECLARED_PROP_TYPE, declaredEntityTypePropInfo.name, sourceType.getName(), declaredEntityTypePropInfo.javaType().getName(), yield.propType.javaType().getName()));
                    }
                    querySourceInfo.addProp(new EntityTypePropInfo<>(yield.name, declaredEntityTypePropInfo.propQuerySourceInfo, declaredEntityTypePropInfo.hibType, yield.required));
                } else {
                    // TODO need to ensure that in case of UE or complex value all declared subprops match yielded ones.
                    // TODO need actual (based on yield) rather than declared info (similar to not declared props section below).
                    querySourceInfo.addProp(declaredProp.hasExpression() ? declaredProp.cloneWithoutExpression() : declaredProp);
                }
            } else {
                // adding not declared props
                querySourceInfo.addProp(yield.propType != null && isEntityType(yield.propType.javaType())
                        ? new EntityTypePropInfo<>(yield.name, domainInfo.getQuerySourceInfo((Class<? extends AbstractEntity<?>>) yield.propType.javaType()), H_ENTITY, yield.required)
                        : new PrimTypePropInfo<>(yield.name, yield.propType != null ? yield.propType.javaType() : null, yield.propType != null ? yield.propType.hibType() : null));
            }
        }

        // including all calc-props, which haven't been yielded explicitly

        // In case of ad-hoc added calc props (e.g. totals) -- they should be included here
        for (final AbstractPropInfo<?> prop : declaredProps.values()) {
            if (prop.hasExpression() && !querySourceInfo.getProps().containsKey(prop.name)) {
                querySourceInfo.addProp(prop);
            }
        }

        return querySourceInfo;
    }
    
    private static QuerySourceInfo<?> obtainQuerySourceInfo(final EqlDomainMetadata domainInfo, final List<SourceQuery2> models, final Class<? extends AbstractEntity<?>> sourceType, final boolean isSyntheticEntity) {
        if (isSyntheticEntity || allGenerated(models)) {
            return domainInfo.getEnhancedQuerySourceInfo(sourceType);
        } else {
            return produceQuerySourceInfoForEntityType(domainInfo, models, sourceType, false);
        }
    }
    
    private static boolean allGenerated(final List<SourceQuery2> models) {
        final boolean allGenerated = true;
        for (SourceQuery2 sourceQuery2 : models) {
            if (!sourceQuery2.yields.allGenerated) {
                return false;
            }
        }
        return allGenerated;
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return isSyntheticEntity ? Set.of(sourceType()) : models.stream().map(el -> el.collectEntityTypes()).flatMap(Set::stream).collect(toSet());
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + models.hashCode();
        result = prime * result + (isSyntheticEntity ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!super.equals(obj)) {
            return false;
        }
        
        if (!(obj instanceof Source1BasedOnSubqueries)) {
            return false;
        }

        final Source1BasedOnSubqueries other = (Source1BasedOnSubqueries) obj;

        return Objects.equals(models, other.models) && Objects.equals(isSyntheticEntity, other.isSyntheticEntity);
    }
}