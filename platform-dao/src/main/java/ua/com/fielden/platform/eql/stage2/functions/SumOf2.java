package ua.com.fielden.platform.eql.stage2.functions;

import java.math.BigDecimal;

import org.hibernate.type.BigDecimalType;

import ua.com.fielden.platform.eql.stage2.TransformationContext;
import ua.com.fielden.platform.eql.stage2.TransformationResult;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.functions.SumOf3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

public class SumOf2 extends SingleOperandFunction2<SumOf3> {
    private final boolean distinct;

    public SumOf2(final ISingleOperand2<? extends ISingleOperand3> operand, final boolean distinct) {
        super(operand);
        this.distinct = distinct;
    }

    @Override
    public Class<BigDecimal> type() {
        return BigDecimal.class;
    }

    @Override
    public Object hibType() {
        return BigDecimalType.INSTANCE;
    }
    
    @Override
    public TransformationResult<SumOf3> transform(final TransformationContext context) {
        final TransformationResult<? extends ISingleOperand3> operandTr = operand.transform(context);
        return new TransformationResult<SumOf3>(new SumOf3(operandTr.item, distinct), operandTr.updatedContext);
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