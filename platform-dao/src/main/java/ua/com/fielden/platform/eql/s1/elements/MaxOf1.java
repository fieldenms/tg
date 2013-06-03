package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;
import ua.com.fielden.platform.eql.s2.elements.MaxOf2;



public class MaxOf1 extends SingleOperandFunction1<MaxOf2> {

    public MaxOf1(final ISingleOperand1<? extends ISingleOperand2> operand) {
	super(operand);
    }

    @Override
    public MaxOf2 transform(final TransformatorToS2 resolver) {
	return new MaxOf2(getOperand().transform(resolver));
    }
}