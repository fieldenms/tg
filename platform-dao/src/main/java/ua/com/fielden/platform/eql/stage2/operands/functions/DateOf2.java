package ua.com.fielden.platform.eql.stage2.operands.functions;


import static ua.com.fielden.platform.eql.meta.PropType.DATE_PROP_TYPE;

import ua.com.fielden.platform.eql.stage2.TransformationContext2;
import ua.com.fielden.platform.eql.stage2.TransformationResult2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.functions.DateOf3;

public class DateOf2 extends SingleOperandFunction2<DateOf3> {

    public DateOf2(final ISingleOperand2<? extends ISingleOperand3> operand) {
        super(operand, DATE_PROP_TYPE);
    }

    @Override
    public TransformationResult2<DateOf3> transform(final TransformationContext2 context) {
        final TransformationResult2<? extends ISingleOperand3> operandTransformationResult = operand.transform(context);
        return new TransformationResult2<>(new DateOf3(operandTransformationResult.item, type), operandTransformationResult.updatedContext);
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