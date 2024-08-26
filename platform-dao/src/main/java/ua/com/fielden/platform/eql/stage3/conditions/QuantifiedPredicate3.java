package ua.com.fielden.platform.eql.stage3.conditions;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.enums.Quantifier;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.queries.SubQuery3;
import ua.com.fielden.platform.meta.IDomainMetadata;

public record QuantifiedPredicate3(ISingleOperand3 leftOperand, ComparisonOperator operator,
                                   Quantifier quantifier, SubQuery3 rightOperand)
        implements ICondition3
{

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        return leftOperand.sql(metadata, dbVersion) + " " + operator + " " + quantifier + " " + rightOperand.sql(
                metadata, dbVersion);
    }

}
