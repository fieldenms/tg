package ua.com.fielden.platform.eql.stage2.operands.functions;

import static ua.com.fielden.platform.eql.meta.PropType.INTEGER_PROP_TYPE;

import ua.com.fielden.platform.eql.stage2.TransformationContext2;
import ua.com.fielden.platform.eql.stage2.TransformationResult2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.functions.CountOf3;

public class CountOf2 extends SingleOperandFunction2<CountOf3> {
    private final boolean distinct;

    public CountOf2(final ISingleOperand2<? extends ISingleOperand3> operand, final boolean distinct) {
        super(operand, INTEGER_PROP_TYPE);
        this.distinct = distinct;
    }

    @Override
    public TransformationResult2<CountOf3> transform(final TransformationContext2 context) {
        final TransformationResult2<? extends ISingleOperand3> operandTr = operand.transform(context);
        return new TransformationResult2<>(new CountOf3(operandTr.item, distinct, type), operandTr.updatedContext);
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