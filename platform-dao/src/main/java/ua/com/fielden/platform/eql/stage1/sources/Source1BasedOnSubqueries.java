package ua.com.fielden.platform.eql.stage1.sources;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.QUERY_BASED;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;

import org.hibernate.type.LongType;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.eql.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.eql.meta.AbstractPropInfo;
import ua.com.fielden.platform.eql.meta.EntityInfo;
import ua.com.fielden.platform.eql.meta.EntityTypePropInfo;
import ua.com.fielden.platform.eql.meta.EqlDomainMetadata;
import ua.com.fielden.platform.eql.meta.PrimTypePropInfo;
import ua.com.fielden.platform.eql.stage1.TransformationContext1;
import ua.com.fielden.platform.eql.stage1.operands.queries.SourceQuery1;
import ua.com.fielden.platform.eql.stage2.etc.Yield2;
import ua.com.fielden.platform.eql.stage2.etc.Yields2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.operands.queries.SourceQuery2;
import ua.com.fielden.platform.eql.stage2.sources.Source2BasedOnSubqueries;

public class Source1BasedOnSubqueries extends AbstractSource1<Source2BasedOnSubqueries> {
    private final List<SourceQuery1> models = new ArrayList<>();
    private final boolean isSyntheticEntity;

    public Source1BasedOnSubqueries(final String alias, final List<SourceQuery1> models, final Integer id, final boolean isSyntheticEntity) {
        super(alias, id);
        this.isSyntheticEntity = isSyntheticEntity;
        this.models.addAll(models);
    }
    
    @Override
    public Source2BasedOnSubqueries transform(final TransformationContext1 context) {
        final List<SourceQuery2> transformedQueries = models.stream().map(m -> m.transform(context)).collect(toList());
        validateYields(transformedQueries);
        return new Source2BasedOnSubqueries(transformedQueries, alias, id, produceEntityInfo(context.domainInfo, transformedQueries, sourceType(), isSyntheticEntity), isSyntheticEntity);
    }
    
    @Override
    public Class<? extends AbstractEntity<?>> sourceType() {
        return models.get(0).resultType;
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
    
    public static <T extends AbstractEntity<?>> EntityInfo<T> produceEntityInfoForDefinedEntityType(final EqlDomainMetadata domainInfo, final Map<String, YieldInfoNode> yieldInfoNodes, final Class<T> sourceType) {
        final EntityInfo<T> entityInfo = new EntityInfo<>(sourceType, QUERY_BASED);
        final SortedMap<String, AbstractPropInfo<?>> declaredProps = domainInfo.getEntityInfo(sourceType).getProps();
        
        if (yieldInfoNodes.size() == 1 && yieldInfoNodes.containsKey(ID)) {
            entityInfo.addProp(declaredProps.get(ID));
        } else {
            for (final Entry<String, YieldInfoNode> yield : yieldInfoNodes.entrySet()) {
                final AbstractPropInfo<?> declaredProp = declaredProps.get(yield.getKey());
                if (declaredProp != null) {
                    //TODO check children
                    entityInfo.addProp(declaredProp.hasExpression() ? declaredProp.cloneWithoutExpression() : declaredProp); 
                } else {
                    // adding not declared props
                    entityInfo.addProp(isEntityType(yield.getValue().javaType)
                            ? new EntityTypePropInfo(yield.getKey(), domainInfo.getEntityInfo((Class<? extends AbstractEntity<?>>) yield.getValue().javaType), LongType.INSTANCE, false /*yield.hasRequiredHint*/)
                                    : new PrimTypePropInfo(yield.getKey(), null/*yield.getValue().hibType*/, yield.getValue().javaType));
                }
            }
            
            // including all calc-props, which haven't been yielded explicitly
            for (final AbstractPropInfo<?> prop : declaredProps.values()) {
                if (prop.hasExpression() && !entityInfo.getProps().containsKey(prop.name)) {
                    entityInfo.addProp(prop);
                }
            }
        }

        return entityInfo;
    }
    
    private static EntityInfo<?> produceEntityInfo(final EqlDomainMetadata domainInfo, final List<SourceQuery2> models, final Class<? extends AbstractEntity<?>> sourceType, final boolean isSyntheticEntity) {
        final Yields2 yields = models.get(0).yields;
        
        if (!EntityAggregates.class.equals(sourceType)) {
            // If all yields (from PE or SE) are generated implicitly, then it will correspond to canonical representation.
            // The same is true if given qry-source based on queries represents SE model (for which canonical representation has been generated and cached during startup).
            if (yields.allGenerated || isSyntheticEntity) {
                return domainInfo.getEnhancedEntityInfo(sourceType);
            } else {
                final Map<String, YieldInfoNode> yieldInfoNodes = YieldInfoNodesGenerator.generate(yields.getYields());
                return produceEntityInfoForDefinedEntityType(domainInfo, yieldInfoNodes, sourceType);
            }
        } else {
            final EntityInfo<EntityAggregates> entityInfo = new EntityInfo<>(EntityAggregates.class, QUERY_BASED);

            for (final Yield2 yield : yields.getYields()) {

                if (yield.operand instanceof Prop2 /*&& !((EntProp2) yield.operand).isCalculated()*/) {
                    if (!yield.alias.contains(".")) {
                        entityInfo.addProp(((Prop2) yield.operand).lastPart().cloneRenamed(yield.alias));    
                    }
                } else {
                    entityInfo.addProp(isEntityType(yield.javaType())
                            ? new EntityTypePropInfo(yield.alias, domainInfo.getEntityInfo((Class<? extends AbstractEntity<?>>) yield.javaType()), LongType.INSTANCE, yield.hasRequiredHint)
                            : new PrimTypePropInfo(yield.alias, null/*yield.operand.hibType()*/, yield.javaType()));
                }
            }
            return entityInfo;
        }
    }
    
    private static void validateYields(final List<SourceQuery2> models) {
        if (models.size() > 1) {
            validateYieldsMatrix(generateYieldMatrixFromQueryModels(models), models.size());    
        }
    }

    private static Map<String, List<Yield2>> generateYieldMatrixFromQueryModels(final List<SourceQuery2> models) {
        final Map<String, List<Yield2>> yieldsMatrix = new HashMap<>();        
        for (final SourceQuery2 entQuery : models) {
            for (final Yield2 yield : entQuery.yields.getYields()) {
                final List<Yield2> foundYields = yieldsMatrix.get(yield.alias);
                if (foundYields != null) {
                    foundYields.add(yield);
                } else {
                    final List<Yield2> newList = new ArrayList<>();
                    newList.add(yield);
                    yieldsMatrix.put(yield.alias, newList);
                }
            }
        }
        return yieldsMatrix;
    }

    private static void validateYieldsMatrix(final Map<String, List<Yield2>> yieldsMatrix, final int modelsCount) {
        for (final Map.Entry<String, List<Yield2>> entry : yieldsMatrix.entrySet()) {
            if (entry.getValue().size() != modelsCount) {
                throw new EqlStage1ProcessingException("Incorrect models used as query source - their result types are different! Alias [" + entry.getKey() + "] has been yielded only " + entry.getValue().size() + " but the models count is " + modelsCount);
            }
        }
    }

}