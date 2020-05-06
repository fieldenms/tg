package ua.com.fielden.platform.eql.stage1.elements.functions;

import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.functions.YearOf2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;

public class YearOf1 extends SingleOperandFunction1<YearOf2> {

    public YearOf1(final ISingleOperand1<? extends ISingleOperand2<?>> operand) {
        super(operand);
    }

    @Override
    public YearOf2 transform(final PropsResolutionContext context) {
        return new YearOf2(operand.transform(context));
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + YearOf1.class.getName().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof YearOf1;
    }      
}