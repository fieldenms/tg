package ua.com.fielden.platform.eql.stage1.operands;

import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.TransformationContext1;
import ua.com.fielden.platform.eql.stage1.queries.SubQuery1;
import ua.com.fielden.platform.eql.stage2.operands.QueryBasedSet2;

public class QueryBasedSet1 implements ISetOperand1<QueryBasedSet2> {
    private final SubQuery1 model;

    public QueryBasedSet1(final SubQuery1 model) {
        this.model = model;
    }

    @Override
    public QueryBasedSet2 transform(final TransformationContext1 context) {
        return new QueryBasedSet2(model.transform(context));
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return model.collectEntityTypes();
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

        if (!(obj instanceof QueryBasedSet1)) {
            return false;
        }
        
        final QueryBasedSet1 other = (QueryBasedSet1) obj;
        
        if (model == null) {
            if (other.model != null) {
                return false;
            }
        } else if (!model.equals(other.model)) {
            return false;
        }

        return Objects.equals(model, other.model);
    }
}