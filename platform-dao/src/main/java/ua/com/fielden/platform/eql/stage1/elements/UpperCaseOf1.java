package ua.com.fielden.platform.eql.stage1.elements;

import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.stage2.elements.UpperCaseOf2;
import ua.com.fielden.platform.eql.stage2.elements.ISingleOperand2;

public class UpperCaseOf1 extends SingleOperandFunction1<UpperCaseOf2> {
    public UpperCaseOf1(final ISingleOperand1<? extends ISingleOperand2> operand) {
        super(operand);
    }

    @Override
    public UpperCaseOf2 transform(final TransformatorToS2 resolver) {
        return new UpperCaseOf2(getOperand().transform(resolver));
    }
}