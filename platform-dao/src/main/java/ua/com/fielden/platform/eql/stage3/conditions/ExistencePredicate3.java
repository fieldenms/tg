package ua.com.fielden.platform.eql.stage3.conditions;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.stage3.queries.SubQueryForExists3;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.ToString;

public record ExistencePredicate3 (boolean negated, SubQueryForExists3 subQuery)
        implements ICondition3, ToString.IFormattable
{

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        return (negated ? "NOT" : "") + " EXISTS (" + subQuery.sql(metadata, dbVersion) + ")";
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines);
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("negated", negated)
                .add("subQuery", subQuery)
                .$();
    }

}
