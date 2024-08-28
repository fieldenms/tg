package ua.com.fielden.platform.eql.stage3.conditions;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.enums.Quantifier;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.queries.SubQuery3;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.ToString;

public record QuantifiedPredicate3(ISingleOperand3 leftOperand, ComparisonOperator operator,
                                   Quantifier quantifier, SubQuery3 rightOperand)
        implements ICondition3, ToString.IFormattable
{

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        return leftOperand.sql(metadata, dbVersion) + " " + operator + " " + quantifier + " " + rightOperand.sql(
                metadata, dbVersion);
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines);
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("left", leftOperand)
                .add("right", rightOperand)
                .add("operand", operator)
                .add("quantifier", quantifier)
                .$();
    }

}
