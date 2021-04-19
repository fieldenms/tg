package ua.com.fielden.platform.eql.stage2.operands.functions;


import java.util.Date;

import ua.com.fielden.platform.eql.stage2.TransformationContext;
import ua.com.fielden.platform.eql.stage2.TransformationResult;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.functions.DateOf3;
import ua.com.fielden.platform.persistence.types.DateTimeType;

public class DateOf2 extends SingleOperandFunction2<DateOf3> {

    public DateOf2(final ISingleOperand2<? extends ISingleOperand3> operand) {
        super(operand, Date.class, DateTimeType.INSTANCE);
    }

    @Override
    public TransformationResult<DateOf3> transform(final TransformationContext context) {
        final TransformationResult<? extends ISingleOperand3> operandTransformationResult = operand.transform(context);
        return new TransformationResult<DateOf3>(new DateOf3(operandTransformationResult.item, type, hibType), operandTransformationResult.updatedContext);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + DateOf2.class.getName().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof DateOf2;
    }
}