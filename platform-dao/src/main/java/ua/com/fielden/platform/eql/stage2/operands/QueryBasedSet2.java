package ua.com.fielden.platform.eql.stage2.operands;

import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.eql.stage2.TransformationContext;
import ua.com.fielden.platform.eql.stage2.TransformationResult;
import ua.com.fielden.platform.eql.stage3.operands.QueryBasedSet3;
import ua.com.fielden.platform.eql.stage3.operands.SubQuery3;

public class QueryBasedSet2 implements ISetOperand2<QueryBasedSet3> {
    private final SubQuery2 model;

    public QueryBasedSet2(final SubQuery2 model) {
        this.model = model;
    }

    @Override
    public TransformationResult<QueryBasedSet3> transform(final TransformationContext context) {
        final TransformationResult<SubQuery3> modelTr = model.transform(context);
        return new TransformationResult<>(new QueryBasedSet3(modelTr.item), modelTr.updatedContext);
    }

    @Override
    public Set<Prop2> collectProps() {
        return model.collectProps();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + model.hashCode();
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