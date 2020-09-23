package ua.com.fielden.platform.eql.stage1.elements.sources;

import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.QUERY_BASED;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.hibernate.type.LongType;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.eql.meta.AbstractPropInfo;
import ua.com.fielden.platform.eql.meta.ComponentTypePropInfo;
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
    private final Map<String, List<Yield1>> yieldsMatrix;

    public QrySource1BasedOnSubqueries(final String alias, final List<SourceQuery1> models, final int contextId) {
        super(alias, contextId);
        if (models.isEmpty()) {
            throw new IllegalArgumentException("Couldn't produce instance of QueryBasedSource due to zero models passed to constructor!");
        }

        this.models.addAll(models);
        this.yieldsMatrix = populateYieldMatrixFromQueryModels(this.models);
        validateYieldsMatrix();
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
    
    private void validateYieldsMatrix() {
        for (final Map.Entry<String, List<Yield1>> entry : yieldsMatrix.entrySet()) {
            if (entry.getValue().size() != models.size()) {
                throw new EqlStage1ProcessingException("Incorrect models used as query source - their result types are different!");
            }
        }
    }
   
    @Override
    public QrySource2BasedOnSubqueries transform(final PropsResolutionContext context) {
        final List<SourceQuery2> transformedQueries = models.stream().map(m -> m.transform(context)).collect(toList());
        return new QrySource2BasedOnSubqueries(transformedQueries, alias, getTransformedContextId(context), produceEntityInfoFrom(context.getDomainInfo(), transformedQueries));
    }
    
    private SourceQuery1 firstModel() {
        return models.get(0);
    }
    
    @Override
    public Class<? extends AbstractEntity<?>> sourceType() {
        return firstModel().resultType;
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
    
    private static Map<String, List<Yield2>> populateYieldMatrixFromQueryModels2(final List<SourceQuery2> models) {
        final Map<String, List<Yield2>> yieldsMatrix = new HashMap<>();
        for (final SourceQuery2 entQuery : models) {
            for (final Yield2 yield : entQuery.yields.getYields()) {
                final List<Yield2> foundYields = yieldsMatrix.get(yield.alias);
                if (foundYields != null) {
                    foundYields.add(yield);
                } else {
                    final List<Yield2> newList = new ArrayList<Yield2>();
                    newList.add(yield);
                    yieldsMatrix.put(yield.alias, newList);
                }
            }
        }

        for (final Map.Entry<String, List<Yield2>> entry : yieldsMatrix.entrySet()) {
            if (entry.getValue().size() != models.size()) {
                throw new IllegalStateException("Incorrect models used as query source - their result types are different!");
            }
        }
        
        return yieldsMatrix;
    }

    private EntityInfo<?> produceEntityInfoFrom(final LongMetadata domainInfo, final List<SourceQuery2> models) {
        final Map<String, List<Yield2>> yieldsMatrix = populateYieldMatrixFromQueryModels2(models);
        final Collection<Yield2> yields = models.get(0).yields.getYields();
        
        if (isPersistedEntityType(sourceType())) {
            if (yields.size() == 1 && yields.iterator().next().alias.equals(ID)) {
                final EntityInfo<?> actualEi = new EntityInfo<>(sourceType(), QUERY_BASED);
                actualEi.addProp(domainInfo.getEntityInfo(sourceType()).getProps().get(ID));
                return actualEi;
            } else {
                return domainInfo.getEntityInfo(sourceType());
            }
        } else if (EntityAggregates.class.equals(sourceType())) {
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
        } else {
            final EntityInfo<?> declaredEi = domainInfo.getEntityInfo(sourceType());
            final EntityInfo<?> actualEi = new EntityInfo<>(sourceType(), QUERY_BASED);
            for (final Entry<String, AbstractPropInfo<?>> declaredProp : declaredEi.getProps().entrySet()) {

                if (declaredProp.getValue().hasExpression()) {
                    actualEi.addProp(declaredProp.getValue());
                } else if (declaredProp.getValue() instanceof ComponentTypePropInfo<?>) {
                    final ComponentTypePropInfo<?> prop = (ComponentTypePropInfo<?>) declaredProp.getValue();
                    for (final String leafPropPath : prop.generateLeafItemsPaths()) {
                        if (yieldsMatrix.containsKey(leafPropPath)) {
                            actualEi.addProp(declaredProp.getValue());
                            break;
                        }
                    }
                } else {
                    if (yieldsMatrix.containsKey(declaredProp.getKey())) {
                        actualEi.addProp(declaredProp.getValue());
                    } else {
                        //        System.out.println("skipping " + declaredProp.getKey() + " in " + sourceType().getSimpleName());
                    }
                }
            }

            for (final Yield2 yield : yields) {
                //if(declaredEi.getProps().containsKey(yield.alias))
            }

            return actualEi;
        }
    }
}