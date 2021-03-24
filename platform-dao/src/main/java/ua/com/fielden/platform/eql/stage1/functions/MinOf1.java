package ua.com.fielden.platform.eql.stage1.functions;

import ua.com.fielden.platform.eql.stage1.TransformationContext;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.functions.MinOf2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;

public class MinOf1 extends SingleOperandFunction1<MinOf2> {

    public MinOf1(final ISingleOperand1<? extends ISingleOperand2<?>> operand) {
        super(operand);
    }

    @Override
    public MinOf2 transform(final TransformationContext context) {
        return new MinOf2(operand.transform(context));
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + MinOf1.class.getName().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof MinOf1;
    }    
}