package ua.com.fielden.platform.eql.stage2.operands.functions;

import ua.com.fielden.platform.eql.stage2.TransformationContext;
import ua.com.fielden.platform.eql.stage2.TransformationResult;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.functions.AverageOf3;

public class AverageOf2 extends SingleOperandFunction2<AverageOf3> {
    public final boolean distinct;

    public AverageOf2(final ISingleOperand2<? extends ISingleOperand3> operand, final boolean distinct) {
        super(operand, operand.type(), operand.hibType());
        this.distinct = distinct;
    }

    @Override
    public TransformationResult<AverageOf3> transform(final TransformationContext context) {
        final TransformationResult<? extends ISingleOperand3> operandTr = operand.transform(context);
        return new TransformationResult<AverageOf3>(new AverageOf3(operandTr.item, distinct, type, hibType), operandTr.updatedContext);
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
        
        if (!(obj instanceof AverageOf2)) {
            return false;
        }
        
        final AverageOf2 other = (AverageOf2) obj;
        
        return distinct == other.distinct;
    }
}