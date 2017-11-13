package ua.com.fielden.platform.eql.stage1.elements;

import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.stage2.elements.LowerCaseOf2;
import ua.com.fielden.platform.eql.stage2.elements.ISingleOperand2;

public class LowerCaseOf1 extends SingleOperandFunction1<LowerCaseOf2> {
    public LowerCaseOf1(final ISingleOperand1<? extends ISingleOperand2> operand) {
        super(operand);
    }

    @Override
    public LowerCaseOf2 transform(final TransformatorToS2 resolver) {
        return new LowerCaseOf2(getOperand().transform(resolver));
    }
}