package ua.com.fielden.platform.eql.stage1.sources;

import com.google.common.collect.ImmutableList;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.eql.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.eql.meta.QuerySourceInfoProvider;
import ua.com.fielden.platform.eql.meta.query.AbstractQuerySourceItem;
import ua.com.fielden.platform.eql.meta.query.QuerySourceInfo;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForEntityType;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForPrimType;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.queries.SourceQuery1;
import ua.com.fielden.platform.eql.stage2.queries.SourceQuery2;
import ua.com.fielden.platform.eql.stage2.sources.Source2BasedOnQueries;
import ua.com.fielden.platform.utils.CollectionUtil;

import java.util.*;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.String.format;
import static java.util.Collections.emptySortedMap;
import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.persistence.HibernateConstants.H_ENTITY;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;

public class Source1BasedOnQueries extends AbstractSource1<Source2BasedOnQueries> {
    public static final String ERR_CONFLICT_BETWEEN_YIELDED_AND_DECLARED_PROP_TYPE = "There is a problem while trying to determine the type for property [%s] of a query source based on queries with result type [%s].\n"
            + "Declared type is [%s].\nActual yield type is [%s].";

    private final List<SourceQuery1> models;
    private final boolean isSyntheticEntity;

    /**
     * @param alias  the alias of this source or {@code null}
     */
    public Source1BasedOnQueries(final List<SourceQuery1> models, final String alias, final Integer id, final Class<? extends AbstractEntity<?>> syntheticEntityType) {
        super(determineSourceType(models, syntheticEntityType), alias, id);
        this.isSyntheticEntity = syntheticEntityType != null;
        this.models = ImmutableList.copyOf(models);
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
    public Source2BasedOnQueries transform(final TransformationContextFromStage1To2 context) {
        final List<SourceQuery2> transformedQueries = models.stream().map(m -> m.transform(context)).collect(toImmutableList());
        final QuerySourceInfo<?> ei = obtainQuerySourceInfo(context.querySourceInfoProvider, transformedQueries, sourceType(), isSyntheticEntity);
        return new Source2BasedOnQueries(transformedQueries, alias, id, ei, isSyntheticEntity, true, context.isForCalcProp);
    }

    public static <T extends AbstractEntity<?>> QuerySourceInfo<T> produceQuerySourceInfoForEntityType(
            final QuerySourceInfoProvider querySourceInfoProvider,
            final List<SourceQuery2> models,
            final Class<T> sourceType,
            final boolean isComprehensive)
    {
        final Map<String, AbstractQuerySourceItem<?>> declaredProps = EntityAggregates.class == sourceType
                ? emptySortedMap()
                : querySourceInfoProvider.getDeclaredQuerySourceInfo(sourceType).getProps();
        final Collection<YieldInfoNode> yieldInfoNodes = YieldInfoNodesGenerator.generate(models);
        final Map<String, AbstractQuerySourceItem<?>> createdProps = new HashMap<>();
        for (final YieldInfoNode yield : yieldInfoNodes) {
            final AbstractQuerySourceItem<?> declaredProp = declaredProps.get(yield.name());
            if (declaredProp != null) {
                // The only thing that has to be taken from declared is its structure (in case of UE or complex value)
                if (declaredProp instanceof QuerySourceItemForEntityType<?> declaredEntityTypeQuerySourceInfoItem) { // here we assume that yield is of ET (this will help to handle the case of yielding ID, which currently is just long only.
                    if (yield.propType().isNull() ||
                        yield.propType().javaType() == declaredEntityTypeQuerySourceInfoItem.javaType() ||
                        yield.propType().javaType() == Long.class
                    ) {
                        createdProps.put(yield.name(), new QuerySourceItemForEntityType<>(yield.name(), querySourceInfoProvider.getModelledQuerySourceInfo((Class<? extends AbstractEntity<?>>) declaredProp.javaType()), declaredEntityTypeQuerySourceInfoItem.hibType, yield.nonnullable()));
                    } else {
                        throw new EqlStage1ProcessingException(
                                format(ERR_CONFLICT_BETWEEN_YIELDED_AND_DECLARED_PROP_TYPE,
                                        declaredEntityTypeQuerySourceInfoItem.name, sourceType.getName(),
                                        declaredEntityTypeQuerySourceInfoItem.javaType().getName(),
                                        yield.propType().javaType().getName()));
                    }
                } else {
                    // TODO need to ensure that in case of UE or complex value all declared subprops match yielded ones.
                    // TODO need actual (based on yield) rather than declared info (similar to not declared props section below).
                    createdProps.put(declaredProp.name, declaredProp.hasExpression() ? declaredProp.cloneWithoutExpression() : declaredProp);
                }
            } else {
                // adding not declared props
                createdProps.put(yield.name(), yield.propType().isNotNull() && isEntityType(yield.propType().javaType())
                        ? new QuerySourceItemForEntityType<>(yield.name(), querySourceInfoProvider.getModelledQuerySourceInfo((Class<? extends AbstractEntity<?>>) yield.propType().javaType()), H_ENTITY, yield.nonnullable())
                        : new QuerySourceItemForPrimType<>(yield.name(), yield.propType().isNotNull() ? yield.propType().javaType() : null, yield.propType().isNotNull() ? yield.propType().hibType() : null));
            }
        }

        // include all calc-props, which haven't been yielded explicitly
        for (final AbstractQuerySourceItem<?> prop : declaredProps.values()) {
            if (prop.hasExpression() && !createdProps.containsKey(prop.name)) {
                createdProps.put(prop.name, prop);
            }
        }

        return new QuerySourceInfo<>(sourceType, isComprehensive, createdProps.values());
    }

    private static QuerySourceInfo<?> obtainQuerySourceInfo(final QuerySourceInfoProvider querySourceInfoProvider, final List<SourceQuery2> models, final Class<? extends AbstractEntity<?>> sourceType, final boolean isSyntheticEntity) {
        if (isSyntheticEntity || allGenerated(models)) {
            return querySourceInfoProvider.getModelledQuerySourceInfo(sourceType);
        } else {
            return produceQuerySourceInfoForEntityType(querySourceInfoProvider, models, sourceType, false);
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

        if (!(obj instanceof Source1BasedOnQueries)) {
            return false;
        }

        final Source1BasedOnQueries other = (Source1BasedOnQueries) obj;

        return Objects.equals(models, other.models) && Objects.equals(isSyntheticEntity, other.isSyntheticEntity);
    }

    @Override
    public String toString() {
        return "Source(%s, alias=%s, id=%s, models=(%s))".formatted(sourceType().getTypeName(), alias, id, CollectionUtil.toString(models, "; "));
    }

}
