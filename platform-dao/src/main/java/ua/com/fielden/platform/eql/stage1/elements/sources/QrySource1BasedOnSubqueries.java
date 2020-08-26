package ua.com.fielden.platform.eql.stage1.elements.sources;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.Yield1;
import ua.com.fielden.platform.eql.stage1.elements.operands.SourceQuery1;
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
        return new QrySource2BasedOnSubqueries(transformedQueries, alias, context.getDomainInfo(), getTransformedContextId(context));
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
}