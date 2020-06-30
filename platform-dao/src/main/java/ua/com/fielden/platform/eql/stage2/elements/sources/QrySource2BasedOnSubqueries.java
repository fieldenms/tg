package ua.com.fielden.platform.eql.stage2.elements.sources;

import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.type.LongType;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.eql.meta.AbstractPropInfo;
import ua.com.fielden.platform.eql.meta.EntityInfo;
import ua.com.fielden.platform.eql.meta.EntityTypePropInfo;
import ua.com.fielden.platform.eql.meta.PrimTypePropInfo;
import ua.com.fielden.platform.eql.stage2.elements.AbstractElement2;
import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.Yield2;
import ua.com.fielden.platform.eql.stage2.elements.Yields2;
import ua.com.fielden.platform.eql.stage2.elements.operands.EntProp2;
import ua.com.fielden.platform.eql.stage2.elements.operands.SourceQuery2;
import ua.com.fielden.platform.eql.stage3.elements.operands.SourceQuery3;
import ua.com.fielden.platform.eql.stage3.elements.sources.QrySource3BasedOnSubqueries;

public class QrySource2BasedOnSubqueries extends AbstractElement2 implements IQrySource2<QrySource3BasedOnSubqueries> {
    private final List<SourceQuery2> models = new ArrayList<>();
    private final Map<String, List<Yield2>> yieldsMatrix;
    private final EntityInfo<?> entityInfo;
    private final String alias;

    public QrySource2BasedOnSubqueries(final List<SourceQuery2> models, final String alias, final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> domainInfo, final String contextId) {
        super(contextId);
        if (models == null || models.isEmpty()) {
            throw new EqlStage1ProcessingException("Couldn't produce instance of QueryBasedSource due to zero models passed to constructor!");
        }

        this.models.addAll(models);
        this.yieldsMatrix = populateYieldMatrixFromQueryModels(models);
        this.alias = alias;
        validateYieldsMatrix();
        this.entityInfo = produceEntityInfoFrom(domainInfo);
        if (entityInfo == null) {
            throw new EqlException("DomainMetadata for type [" + sourceType() + "] is missing.");
        }
    }

    private static Map<String, List<Yield2>> populateYieldMatrixFromQueryModels(final List<SourceQuery2> models) {
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
        return yieldsMatrix;
    }

    private SourceQuery2 firstModel() {
        return models.get(0);
    }

    private void validateYieldsMatrix() {
        for (final Map.Entry<String, List<Yield2>> entry : yieldsMatrix.entrySet()) {
            if (entry.getValue().size() != models.size()) {
                throw new IllegalStateException("Incorrect models used as query source - their result types are different!");
            }
        }
    }

    @Override
    public Class<? extends AbstractEntity<?>> sourceType() {
        return (Class<? extends AbstractEntity<?>>) firstModel().resultType;
    }

    public Yields2 getYields() {
        return firstModel().yields;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + models.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof QrySource2BasedOnSubqueries)) {
            return false;
        }
        final QrySource2BasedOnSubqueries other = (QrySource2BasedOnSubqueries) obj;
        if (models == null) {
            if (other.models != null) {
                return false;
            }
        } else if (!models.equals(other.models)) {
            return false;
        }
        return true;
    }

    @Override
    public EntityInfo<?> entityInfo() {
        return entityInfo;
    }
    
    private EntityInfo<?> produceEntityInfoFrom(final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> domainInfo) {
        if (!EntityAggregates.class.equals(sourceType())) {
            return domainInfo.get(sourceType());
        } else {
            final EntityInfo<EntityAggregates> entAggEntityInfo = new EntityInfo<>(EntityAggregates.class, null);
            for (final Yield2 yield : getYields().getYields()) {
                final AbstractPropInfo<?> aep = isEntityType(yield.javaType())
                        ? new EntityTypePropInfo(yield.alias, domainInfo.get(yield.javaType()), LongType.INSTANCE, yield.hasRequiredHint)
                        : new PrimTypePropInfo(yield.alias, yield.operand.hibType(), yield.javaType());
                entAggEntityInfo.addProp(aep);
            }
            return entAggEntityInfo;
        }
    }

    @Override
    public String alias() {
        return alias;
    }

    @Override
    public String contextId() {
        return contextId;
    }

    @Override
    public TransformationResult<QrySource3BasedOnSubqueries> transform(final TransformationContext context) {
        
        final List<SourceQuery3> transformedQueries = new ArrayList<>();
        TransformationContext currentResolutionContext = context;

        for (final SourceQuery2 model : models) {
            final TransformationResult<SourceQuery3> modelTr = model.transform(currentResolutionContext/*.produceNewOne() // as already invoked as part of EntQuery1.transform(..)*/);
            transformedQueries.add(modelTr.item);
            currentResolutionContext = modelTr.updatedContext; // TODO should be just resolutionContext with propsResolutions added from this model transformation   
        }
           
        final QrySource3BasedOnSubqueries transformedSource = new QrySource3BasedOnSubqueries(transformedQueries, contextId);//resolutionContext.getDomainInfo());
        return new TransformationResult<QrySource3BasedOnSubqueries>(transformedSource, currentResolutionContext);
    }

    @Override
    public Set<EntProp2> collectProps() {
        final Set<EntProp2> result = new HashSet<>();
        for (final SourceQuery2 model : models) {
            result.addAll(model.collectProps());
        }
        return result;
    }
}