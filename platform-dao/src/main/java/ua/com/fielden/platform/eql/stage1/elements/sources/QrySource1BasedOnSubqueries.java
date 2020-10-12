package ua.com.fielden.platform.eql.stage1.elements.sources;

import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.QUERY_BASED;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.SortedMap;

import org.hibernate.type.LongType;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.eql.meta.AbstractPropInfo;
import ua.com.fielden.platform.eql.meta.EntityInfo;
import ua.com.fielden.platform.eql.meta.EntityTypePropInfo;
import ua.com.fielden.platform.eql.meta.LongMetadata;
import ua.com.fielden.platform.eql.meta.PrimTypePropInfo;
import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.Yield1;
import ua.com.fielden.platform.eql.stage1.elements.operands.SourceQuery1;
import ua.com.fielden.platform.eql.stage2.elements.Yield2;
import ua.com.fielden.platform.eql.stage2.elements.operands.EntProp2;
import ua.com.fielden.platform.eql.stage2.elements.operands.SourceQuery2;
import ua.com.fielden.platform.eql.stage2.elements.sources.QrySource2BasedOnSubqueries;

public class QrySource1BasedOnSubqueries extends AbstractQrySource1<QrySource2BasedOnSubqueries> {
    private final List<SourceQuery1> models = new ArrayList<>();

    public QrySource1BasedOnSubqueries(final String alias, final List<SourceQuery1> models, final int contextId) {
        super(alias, contextId);
        if (models.isEmpty()) {
            throw new IllegalArgumentException("Couldn't produce instance of QueryBasedSource due to zero models passed to constructor!");
        }

        this.models.addAll(models);
        validateYieldsMatrix(populateYieldMatrixFromQueryModels(this.models), this.models.size());
    }
    
    private static Map<String, List<Yield1>> populateYieldMatrixFromQueryModels(final List<SourceQuery1> models) {
        final Map<String, List<Yield1>> yieldsMatrix = new HashMap<>();        
        for (final SourceQuery1 entQuery : models) {
            for (final Yield1 yield : entQuery.yields.getYields()) {
                final List<Yield1> foundYields = yieldsMatrix.get(yield.alias);
                if (foundYields != null) {
                    foundYields.add(yield);
                } else {
                    final List<Yield1> newList = new ArrayList<>();
                    newList.add(yield);
                    yieldsMatrix.put(yield.alias, newList);
                }
            }
        }
        return yieldsMatrix;
    }

    private static void validateYieldsMatrix(final Map<String, List<Yield1>> yieldsMatrix, final int modelsCount) {
        for (final Map.Entry<String, List<Yield1>> entry : yieldsMatrix.entrySet()) {
            if (entry.getValue().size() != modelsCount) {
                throw new EqlStage1ProcessingException("Incorrect models used as query source - their result types are different!");
            }
        }
    }
   
    @Override
    public QrySource2BasedOnSubqueries transform(final PropsResolutionContext context) {
        final List<SourceQuery2> transformedQueries = models.stream().map(m -> m.transform(context)).collect(toList());
        return new QrySource2BasedOnSubqueries(transformedQueries, alias, getTransformedContextId(context), produceEntityInfo(context.getDomainInfo(), transformedQueries, sourceType()));
    }
    
    @Override
    public Class<? extends AbstractEntity<?>> sourceType() {
        return models.get(0).resultType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + models.hashCode();
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
        
        if (!(obj instanceof QrySource1BasedOnSubqueries)) {
            return false;
        }

        final QrySource1BasedOnSubqueries other = (QrySource1BasedOnSubqueries) obj;

        return Objects.equals(models, other.models);
    }
    
    private static EntityInfo<?> produceEntityInfoForDefinedEntityType(final LongMetadata domainInfo, final Map<String, YieldInfoNode> yieldInfoNodes, final Class<? extends AbstractEntity<?>> sourceType) {
        final EntityInfo<? extends AbstractEntity<?>> entityInfo = new EntityInfo<>(sourceType, QUERY_BASED);
        final SortedMap<String, AbstractPropInfo<?>> declaredProps = domainInfo.getEntityInfo(sourceType).getProps();
        
        if (yieldInfoNodes.size() == 1 && yieldInfoNodes.containsKey(ID)) {
            entityInfo.addProp(declaredProps.get(ID));
        } else {
            for (final Entry<String, YieldInfoNode> yield : yieldInfoNodes.entrySet()) {
                if (declaredProps.containsKey(yield.getKey())) {
                    //TODO check children
                    entityInfo.addProp(declaredProps.get(yield.getKey()));
                } else {
                    entityInfo.addProp(isEntityType(yield.getValue().javaType)
                            ? new EntityTypePropInfo(yield.getKey(), domainInfo.getEntityInfo((Class<? extends AbstractEntity<?>>) yield.getValue().javaType), LongType.INSTANCE, false /*yield.hasRequiredHint*/)
                                    : new PrimTypePropInfo(yield.getKey(), yield.getValue().hibType, yield.getValue().javaType));
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
    
    private static EntityInfo<?> produceEntityInfo(final LongMetadata domainInfo, final List<SourceQuery2> models, final Class<? extends AbstractEntity<?>> sourceType) {
        final Collection<Yield2> yields = models.get(0).yields.getYields();
        final Map<String, YieldInfoNode> yieldInfoNodes = YieldInfoNodesGenerator.generate(yields);
        
        if (!EntityAggregates.class.equals(sourceType)) {
            return produceEntityInfoForDefinedEntityType(domainInfo, yieldInfoNodes, sourceType);            
        } else {
            final EntityInfo<EntityAggregates> entityInfo = new EntityInfo<>(EntityAggregates.class, QUERY_BASED);

            for (final Yield2 yield : yields) {

                if (yield.operand instanceof EntProp2 /*&& !((EntProp2) yield.operand).isCalculated()*/) {
                    if (!yield.alias.contains(".")) {
                        entityInfo.addProp(((EntProp2) yield.operand).lastPart().cloneRenamed(yield.alias));    
                    }
                } else {
                    entityInfo.addProp(isEntityType(yield.javaType())
                            ? new EntityTypePropInfo(yield.alias, domainInfo.getEntityInfo((Class<? extends AbstractEntity<?>>) yield.javaType()), LongType.INSTANCE, yield.hasRequiredHint)
                            : new PrimTypePropInfo(yield.alias, yield.operand.hibType(), yield.javaType()));
                }
            }
            return entityInfo;
        }
    }
}