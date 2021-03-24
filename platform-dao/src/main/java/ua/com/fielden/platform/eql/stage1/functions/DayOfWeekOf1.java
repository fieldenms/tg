package ua.com.fielden.platform.eql.stage1.functions;

import ua.com.fielden.platform.eql.stage1.TransformationContext;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.functions.DayOfWeekOf2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;

public class DayOfWeekOf1 extends SingleOperandFunction1<DayOfWeekOf2> {

    public DayOfWeekOf1(final ISingleOperand1<? extends ISingleOperand2<?>> operand) {
        super(operand);
    }

    @Override
    public DayOfWeekOf2 transform(final TransformationContext context) {
        return new DayOfWeekOf2(operand.transform(context));
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + DayOfWeekOf1.class.getName().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof DayOfWeekOf1; 
    }
}