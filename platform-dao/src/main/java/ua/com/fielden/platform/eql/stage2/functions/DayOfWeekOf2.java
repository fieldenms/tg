package ua.com.fielden.platform.eql.stage2.functions;

import org.hibernate.type.IntegerType;

import ua.com.fielden.platform.eql.stage2.TransformationContext;
import ua.com.fielden.platform.eql.stage2.TransformationResult;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.functions.DayOfWeekOf3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

public class DayOfWeekOf2 extends SingleOperandFunction2<DayOfWeekOf3> {

    public DayOfWeekOf2(final ISingleOperand2<? extends ISingleOperand3> operand) {
        super(operand, Integer.class, IntegerType.INSTANCE);
    }

    @Override
    public TransformationResult<DayOfWeekOf3> transform(final TransformationContext context) {
        final TransformationResult<? extends ISingleOperand3> operandTransformationResult = operand.transform(context);
        return new TransformationResult<DayOfWeekOf3>(new DayOfWeekOf3(operandTransformationResult.item, type, hibType), operandTransformationResult.updatedContext);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + DayOfWeekOf2.class.getName().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof DayOfWeekOf2; 
    }
}