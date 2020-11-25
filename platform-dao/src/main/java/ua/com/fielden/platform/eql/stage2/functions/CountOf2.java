package ua.com.fielden.platform.eql.stage2.functions;

import java.math.BigInteger;

import org.hibernate.type.BigIntegerType;

import ua.com.fielden.platform.eql.stage2.TransformationContext;
import ua.com.fielden.platform.eql.stage2.TransformationResult;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.functions.CountOf3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

public class CountOf2 extends SingleOperandFunction2<CountOf3> {
    private final boolean distinct;

    public CountOf2(final ISingleOperand2<? extends ISingleOperand3> operand, final boolean distinct) {
        super(operand);
        this.distinct = distinct;
    }

    @Override
    public Class<BigInteger> type() {
        return BigInteger.class;
    }

    @Override
    public Object hibType() {
        return BigIntegerType.INSTANCE;
    }

    @Override
    public TransformationResult<CountOf3> transform(final TransformationContext context) {
        final TransformationResult<? extends ISingleOperand3> operandTr = operand.transform(context);
        return new TransformationResult<CountOf3>(new CountOf3(operandTr.item, distinct), operandTr.updatedContext);
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
        
        if (!(obj instanceof CountOf2)) {
            return false;
        }
        
        final CountOf2 other = (CountOf2) obj;
        
        return distinct == other.distinct;
    }
}