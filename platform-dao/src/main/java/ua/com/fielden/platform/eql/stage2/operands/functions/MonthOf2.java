package ua.com.fielden.platform.eql.stage2.operands.functions;

import ua.com.fielden.platform.eql.stage2.TransformationContext;
import ua.com.fielden.platform.eql.stage2.TransformationResult;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.functions.MonthOf3;

public class MonthOf2 extends DatePartFunction2<MonthOf3> {

    public MonthOf2(final ISingleOperand2<? extends ISingleOperand3> operand) {
        super(operand);
    }

    @Override
    public TransformationResult<MonthOf3> transform(final TransformationContext context) {
        final TransformationResult<? extends ISingleOperand3> operandTransformationResult = operand.transform(context);
        return new TransformationResult<>(new MonthOf3(operandTransformationResult.item, type, hibType), operandTransformationResult.updatedContext);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + MonthOf2.class.getName().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof MonthOf2;
    }  
}