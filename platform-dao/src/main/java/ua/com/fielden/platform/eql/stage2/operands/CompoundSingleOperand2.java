package ua.com.fielden.platform.eql.stage2.operands;

import ua.com.fielden.platform.entity.query.fluent.enums.ArithmeticalOperator;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.utils.ToString;

public record CompoundSingleOperand2 (ISingleOperand2<? extends ISingleOperand3> operand,
                                      ArithmeticalOperator operator)
    implements ToString.IFormattable
{

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
