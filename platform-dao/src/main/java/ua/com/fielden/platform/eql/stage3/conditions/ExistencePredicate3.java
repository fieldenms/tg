package ua.com.fielden.platform.eql.stage3.conditions;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.stage3.queries.SubQueryForExists3;
import ua.com.fielden.platform.meta.IDomainMetadata;

public record ExistencePredicate3 (boolean negated, SubQueryForExists3 subQuery)
        implements ICondition3
{

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        return (negated ? "NOT" : "") + " EXISTS (" + subQuery.sql(metadata, dbVersion) + ")";
    }

}
