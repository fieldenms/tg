package ua.com.fielden.platform.eql.stage1.operands.functions;

import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.functions.SumOf2;

public class SumOf1 extends SingleOperandFunction1<SumOf2> {
    private final boolean distinct;

    public SumOf1(final ISingleOperand1<? extends ISingleOperand2<?>> operand, final boolean distinct) {
        super(operand);
        this.distinct = distinct;
    }

    @Override
    public SumOf2 transform(final TransformationContextFromStage1To2 context) {
        return new SumOf2(operand.transform(context), distinct);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + (distinct ? 1231 : 1237);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!super.equals(obj)) {
            return false;
        }
        
        if (!(obj instanceof SumOf1)) {
            return false;
        }
        
        final SumOf1 other = (SumOf1) obj;
        
        return distinct == other.distinct;
    }
}