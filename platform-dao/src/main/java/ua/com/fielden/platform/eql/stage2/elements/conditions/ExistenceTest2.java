package ua.com.fielden.platform.eql.stage2.elements.conditions;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage2.elements.operands.EntQuery2;

public class ExistenceTest2 extends AbstractCondition2 {
    private final boolean negated;
    private final EntQuery2 subQuery;

    public ExistenceTest2(final boolean negated, final EntQuery2 subQuery) {
        this.negated = negated;
        this.subQuery = subQuery;
    }

    @Override
    public boolean ignore() {
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (negated ? 1231 : 1237);
        result = prime * result + ((subQuery == null) ? 0 : subQuery.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
     
        if (!(obj instanceof ExistenceTest2)) {
            return false;
        }
        
        final ExistenceTest2 other = (ExistenceTest2) obj;
        
        return Objects.equals(negated, other.negated) && Objects.equals(subQuery, other.subQuery);
    }
}