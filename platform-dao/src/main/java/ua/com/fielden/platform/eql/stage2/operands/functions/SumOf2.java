package ua.com.fielden.platform.eql.stage2.operands.functions;

import ua.com.fielden.platform.eql.stage2.TransformationContext2;
import ua.com.fielden.platform.eql.stage2.TransformationResult2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.functions.SumOf3;

public class SumOf2 extends SingleOperandFunction2<SumOf3> {
    private final boolean distinct;

    public SumOf2(final ISingleOperand2<? extends ISingleOperand3> operand, final boolean distinct) {
        super(operand, operand.type(), operand.hibType());
        this.distinct = distinct;
    }

    @Override
    public TransformationResult2<SumOf3> transform(final TransformationContext2 context) {
        final TransformationResult2<? extends ISingleOperand3> operandTr = operand.transform(context);
        return new TransformationResult2<>(new SumOf3(operandTr.item, distinct, type, hibType), operandTr.updatedContext);
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
        
        if (!(obj instanceof SumOf2)) {
            return false;
        }
        
        final SumOf2 other = (SumOf2) obj;
        
        return distinct == other.distinct;
    }
}