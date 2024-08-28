package ua.com.fielden.platform.eql.stage1.sundries;

import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.sundries.OrderBy2;
import ua.com.fielden.platform.utils.ToString;

public record OrderBy1 (ISingleOperand1<? extends ISingleOperand2<?>> operand,
                        String yieldName,
                        boolean isDesc)
    implements ToString.IFormattable
{

    public OrderBy1(final ISingleOperand1<? extends ISingleOperand2<?>> operand, final boolean isDesc) {
        this(operand, null, isDesc);
    }

    public OrderBy1(final String yieldName, final boolean isDesc) {
        this(null, yieldName, isDesc);
    }

    public OrderBy2 transform(final TransformationContextFromStage1To2 context) {
        return operand != null ? new OrderBy2(operand.transform(context), isDesc) : new OrderBy2(yieldName, isDesc);
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("operand", operand)
                .add("yieldName", yieldName)
                .add("isDesc", isDesc)
                .$();
    }

}
