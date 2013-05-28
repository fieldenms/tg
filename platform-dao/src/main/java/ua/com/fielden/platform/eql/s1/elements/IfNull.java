package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;



public class IfNull extends TwoOperandsFunction<ua.com.fielden.platform.eql.s2.elements.IfNull> {

    public IfNull(final ISingleOperand<? extends ISingleOperand2> operand1, final ISingleOperand<? extends ISingleOperand2> operand2) {
	super(operand1, operand2);
    }

    @Override
    public ua.com.fielden.platform.eql.s2.elements.IfNull transform(TransformatorToS2 resolver) {
	return new ua.com.fielden.platform.eql.s2.elements.IfNull(getOperand1().transform(null), getOperand2().transform(null));
    }
}