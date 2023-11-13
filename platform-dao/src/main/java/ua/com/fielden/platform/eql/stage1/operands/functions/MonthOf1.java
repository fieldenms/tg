package ua.com.fielden.platform.eql.stage1.operands.functions;

import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.functions.MonthOf2;

public class MonthOf1 extends SingleOperandFunction1<MonthOf2> {

    public MonthOf1(final ISingleOperand1<? extends ISingleOperand2<?>> operand) {
        super(operand);
    }

    @Override
    public MonthOf2 transform(final TransformationContextFromStage1To2 context) {
        return new MonthOf2(operand.transform(context));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + MonthOf1.class.getName().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof MonthOf1;
    }   
}