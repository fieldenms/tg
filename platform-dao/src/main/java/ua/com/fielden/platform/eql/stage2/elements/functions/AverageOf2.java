package ua.com.fielden.platform.eql.stage2.elements.functions;

import java.math.BigDecimal;

import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.elements.functions.AverageOf3;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class AverageOf2 extends SingleOperandFunction2<AverageOf3> {
    public final boolean distinct;

    public AverageOf2(final ISingleOperand2<? extends ISingleOperand3> operand, final boolean distinct) {
        super(operand);
        this.distinct = distinct;
    }

    @Override
    public Class<BigDecimal> type() {
        return BigDecimal.class;
    }

    @Override
    public TransformationResult<AverageOf3> transform(final TransformationContext context) {
        final TransformationResult<? extends ISingleOperand3> operandTransformationResult = operand.transform(context);
        return new TransformationResult<AverageOf3>(new AverageOf3(operandTransformationResult.item, distinct), operandTransformationResult.updatedContext);
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