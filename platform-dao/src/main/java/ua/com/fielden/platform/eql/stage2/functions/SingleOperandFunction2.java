package ua.com.fielden.platform.eql.stage2.functions;

import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

abstract class SingleOperandFunction2<S3 extends ISingleOperand3> extends AbstractFunction2<S3> {

    public final ISingleOperand2<? extends ISingleOperand3> operand;

    public SingleOperandFunction2(final ISingleOperand2<? extends ISingleOperand3> operand, final Class<?> type, final Object hibType) {
        super(type, hibType);
        this.operand = operand;
    }

    @Override
    public Set<Prop2> collectProps() {
        return operand.collectProps();
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + operand.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof SingleOperandFunction2)) {
            return false;
        }
        
        final SingleOperandFunction2<S3> other = (SingleOperandFunction2<S3>) obj;
        
        return Objects.equals(operand, other.operand);
    }
}