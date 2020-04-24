package ua.com.fielden.platform.eql.stage3.elements.conditions;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.stage3.elements.operands.SubQuery3;

public class ExistenceTest3 implements ICondition3 {
    private final boolean negated;
    private final SubQuery3 subQuery;

    public ExistenceTest3(final boolean negated, final SubQuery3 subQuery) {
        this.negated = negated;
        this.subQuery = subQuery;
    }

    @Override
    public String sql(final DbVersion dbVersion) {
        return (negated ? "NOT" : "") + " EXISTS (" + subQuery.sql(dbVersion) + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (negated ? 1231 : 1237);
        result = prime * result + subQuery.hashCode();
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
        
        return (negated == other.negated) && Objects.equals(subQuery, other.subQuery);
    }
}