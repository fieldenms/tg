package ua.com.fielden.platform.eql.stage2.elements.operands;

import com.google.common.base.Objects;

public class QueryBasedSet2 implements ISetOperand2 {
    private final EntQuery2 model;

    public QueryBasedSet2(final EntQuery2 model) {
        this.model = model;
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
        
        return Objects.equal(model, other.model);
    }
}