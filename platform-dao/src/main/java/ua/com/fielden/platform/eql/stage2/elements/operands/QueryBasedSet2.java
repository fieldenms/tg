package ua.com.fielden.platform.eql.stage2.elements.operands;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage3.elements.operands.EntQuery3;
import ua.com.fielden.platform.eql.stage3.elements.operands.QueryBasedSet3;

public class QueryBasedSet2 implements ISetOperand2<QueryBasedSet3> {
    private final EntQuery2 model;

    public QueryBasedSet2(final EntQuery2 model) {
        this.model = model;
    }

    @Override
    public TransformationResult<QueryBasedSet3> transform(final TransformationContext context) {
        final TransformationResult<EntQuery3> modelTransformationResult = model.transform(context);
        return new TransformationResult<QueryBasedSet3>(new QueryBasedSet3(modelTransformationResult.item), modelTransformationResult.updatedContext);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((model == null) ? 0 : model.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof QueryBasedSet2)) {
            return false;
        }
        
        final QueryBasedSet2 other = (QueryBasedSet2) obj;
        
        return Objects.equals(model, other.model);
    }
}