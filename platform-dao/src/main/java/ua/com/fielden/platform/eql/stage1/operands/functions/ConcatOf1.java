package ua.com.fielden.platform.eql.stage1.operands.functions;

import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage1.operands.Value1;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.functions.ConcatOf2;

import static ua.com.fielden.platform.entity.exceptions.InvalidArgumentException.requireNotNullArgument;

public class ConcatOf1 extends TwoOperandsFunction1<ConcatOf2> {

    public ConcatOf1(
            final ISingleOperand1<? extends ISingleOperand2<?>> expr,
            final Value1 separator)
    {
        super(expr, separator);
        requireNotNullArgument(separator, "separator");
    }

    @Override
    public ConcatOf2 transform(final TransformationContextFromStage1To2 context) {
        return new ConcatOf2(operand1.transform(context), operand2.transform(context));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + ConcatOf1.class.getName().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
               || obj instanceof ConcatOf1 that
                  && super.equals(that);
    }

}
