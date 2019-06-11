package ua.com.fielden.platform.eql.stage1.elements.sources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.eql.meta.PropsResolutionContext;
import ua.com.fielden.platform.eql.meta.TransformationResult;
import ua.com.fielden.platform.eql.stage1.elements.Yield1;
import ua.com.fielden.platform.eql.stage1.elements.operands.EntQuery1;
import ua.com.fielden.platform.eql.stage2.elements.operands.EntQuery2;
import ua.com.fielden.platform.eql.stage2.elements.sources.QrySource2BasedOnSubqueries;

public class QrySource1BasedOnSubqueries extends AbstractQrySource1<QrySource2BasedOnSubqueries> {
    private final List<EntQuery1> models = new ArrayList<>();
    private final Map<String, List<Yield1>> yieldsMatrix;

    public QrySource1BasedOnSubqueries(final String alias, final List<EntQuery1> models, final int contextId) {
        super(alias, contextId);
        if (models == null || models.isEmpty()) {
            throw new IllegalArgumentException("Couldn't produce instance of QueryBasedSource due to zero models passed to constructor!");
        }

        this.models.addAll(models);
        this.yieldsMatrix = populateYieldMatrixFromQueryModels(this.models);
        validateYieldsMatrix();
    }
    
    private static Map<String, List<Yield1>> populateYieldMatrixFromQueryModels(final List<EntQuery1> models) {
        final Map<String, List<Yield1>> yieldsMatrix = new HashMap<>();        
        for (final EntQuery1 entQuery : models) {
            for (final Yield1 yield : entQuery.yields.getYields()) {
                final List<Yield1> foundYields = yieldsMatrix.get(yield.getAlias());
                if (foundYields != null) {
                    foundYields.add(yield);
                } else {
                    final List<Yield1> newList = new ArrayList<>();
                    newList.add(yield);
                    yieldsMatrix.put(yield.getAlias(), newList);
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
    public TransformationResult<QrySource2BasedOnSubqueries> transform(PropsResolutionContext resolutionContext) {
        
        final List<EntQuery2> transformedQueries = new ArrayList<>();
        PropsResolutionContext currentResolutionContext = resolutionContext;

        for (final EntQuery1 model : models) {
            TransformationResult<EntQuery2> modelTransformationResult = model.transform(currentResolutionContext/*.produceNewOne() // as already invoked as part of EntQuery1.transform(..)*/);
            transformedQueries.add(modelTransformationResult.getItem());
            currentResolutionContext = modelTransformationResult.getUpdatedContext(); // TODO should be just resolutionContext with propsResolutions added from this model transformation   
        }
           
        final QrySource2BasedOnSubqueries transformedSource = new QrySource2BasedOnSubqueries(transformedQueries, alias, resolutionContext.getDomainInfo());
        return new TransformationResult<QrySource2BasedOnSubqueries>(transformedSource, /*currentResolutionContext*/resolutionContext.cloneWithAdded(transformedSource, currentResolutionContext.getResolvedProps()));
    }
    
    private EntQuery1 firstModel() {
        return models.get(0);
    }
    
    @Override
    public Class<? extends AbstractEntity<?>> sourceType() {
        return firstModel().type();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((models == null) ? 0 : models.hashCode());
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
        if (models == null) {
            if (other.models != null) {
                return false;
            }
        } else if (!models.equals(other.models)) {
            return false;
        }
        return true;
    }

    public List<EntQuery1> getModels() {
        return models;
    }
}