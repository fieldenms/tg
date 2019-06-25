package ua.com.fielden.platform.eql.stage3.elements.conditions;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage3.elements.operands.EntQuery3;

public class ExistenceTest3 implements ICondition3 {
    private final boolean negated;
    private final EntQuery3 subQuery;

    public ExistenceTest3(final boolean negated, final EntQuery3 subQuery) {
        this.negated = negated;
        this.subQuery = subQuery;
    }

    @Override
    public String sql() {
        return (negated ? "NOT EXISTS " : "EXISTS ") + subQuery.sql();
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
     
        if (!(obj instanceof ExistenceTest3)) {
            return false;
        }
        
        final ExistenceTest3 other = (ExistenceTest3) obj;
        
        return Objects.equals(negated, other.negated) && Objects.equals(subQuery, other.subQuery);
    }
}