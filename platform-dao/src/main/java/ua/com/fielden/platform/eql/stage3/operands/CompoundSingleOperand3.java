package ua.com.fielden.platform.eql.stage3.operands;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.fluent.enums.ArithmeticalOperator;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.ToString;

public record CompoundSingleOperand3 (ISingleOperand3 operand, ArithmeticalOperator operator) implements ToString.IFormattable {

    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        return operator.value + operand.sql(metadata, dbVersion);
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines);
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("operand", operand)
                .add("operator", operator)
                .$();
    }


}
