package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;



public class SecondOf extends SingleOperandFunction<ua.com.fielden.platform.eql.s2.elements.SecondOf> {

    public SecondOf(final ISingleOperand<? extends ISingleOperand2> operand) {
	super(operand);
    }

    @Override
    public ua.com.fielden.platform.eql.s2.elements.SecondOf transform(TransformatorToS2 resolver) {
	return new ua.com.fielden.platform.eql.s2.elements.SecondOf(getOperand().transform(null));
    }
}