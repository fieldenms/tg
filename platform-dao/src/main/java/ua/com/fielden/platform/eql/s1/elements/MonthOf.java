package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;



public class MonthOf extends SingleOperandFunction<ua.com.fielden.platform.eql.s2.elements.MonthOf> {

    public MonthOf(final ISingleOperand<? extends ISingleOperand2> operand) {
	super(operand);
    }

    @Override
    public ua.com.fielden.platform.eql.s2.elements.MonthOf transform(TransformatorToS2 resolver) {
	return new ua.com.fielden.platform.eql.s2.elements.MonthOf(getOperand().transform(null));
    }
}