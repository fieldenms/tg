package ua.com.fielden.platform.eql.stage3.conditions;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.stage3.queries.SubQueryForExists3;
import ua.com.fielden.platform.meta.IDomainMetadata;

import java.util.Objects;

public class ExistencePredicate3 implements ICondition3 {
    private final boolean negated;
    private final SubQueryForExists3 subQuery;

    public ExistencePredicate3(final boolean negated, final SubQueryForExists3 subQuery) {
        this.negated = negated;
        this.subQuery = subQuery;
    }

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        return (negated ? "NOT" : "") + " EXISTS (" + subQuery.sql(metadata, dbVersion) + ")";
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

        if (!(obj instanceof ExistencePredicate3)) {
            return false;
        }

        final ExistencePredicate3 other = (ExistencePredicate3) obj;

        return (negated == other.negated) && Objects.equals(subQuery, other.subQuery);
    }

}
