package ua.com.fielden.platform.eql.stage1.operands;

import ua.com.fielden.platform.entity.query.fluent.enums.ArithmeticalOperator;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage2.operands.CompoundSingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.utils.ToString;

public record CompoundSingleOperand1 (ISingleOperand1<? extends ISingleOperand2<?>> operand,
                                      ArithmeticalOperator operator)
    implements ToString.IFormattable
{

    public CompoundSingleOperand2 transform(final TransformationContextFromStage1To2 context) {
        return new CompoundSingleOperand2(operand.transform(context), operator);
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
