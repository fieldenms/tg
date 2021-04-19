package ua.com.fielden.platform.eql.stage2.functions;

import org.hibernate.type.StringType;

import ua.com.fielden.platform.eql.stage2.TransformationContext;
import ua.com.fielden.platform.eql.stage2.TransformationResult;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.functions.LowerCaseOf3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

public class LowerCaseOf2 extends SingleOperandFunction2<LowerCaseOf3> {
    public LowerCaseOf2(final ISingleOperand2<? extends ISingleOperand3> operand) {
        super(operand, String.class, StringType.INSTANCE);
    }

    @Override
    public TransformationResult<LowerCaseOf3> transform(final TransformationContext context) {
        final TransformationResult<? extends ISingleOperand3> operandTransformationResult = operand.transform(context);
        return new TransformationResult<LowerCaseOf3>(new LowerCaseOf3(operandTransformationResult.item, type, hibType), operandTransformationResult.updatedContext);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + LowerCaseOf2.class.getName().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof LowerCaseOf2;
    }
}