package ua.com.fielden.platform.eql.stage1.functions;

import ua.com.fielden.platform.eql.stage1.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.functions.SecondOf2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;

public class SecondOf1 extends SingleOperandFunction1<SecondOf2> {

    public SecondOf1(final ISingleOperand1<? extends ISingleOperand2<?>> operand) {
        super(operand);
    }

    @Override
    public SecondOf2 transform(final PropsResolutionContext context) {
        return new SecondOf2(operand.transform(context));
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + SecondOf1.class.getName().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof SecondOf1;
    }   
}