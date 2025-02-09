package ua.com.fielden.platform.eql.stage1.operands.functions;

import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;

public abstract class SingleOperandFunction1<T extends ISingleOperand2<?>> extends AbstractFunction1<T> {

    public final ISingleOperand1<? extends ISingleOperand2<?>> operand;

    public SingleOperandFunction1(final ISingleOperand1<? extends ISingleOperand2<?>> operand) {
        this.operand = operand;
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return operand.collectEntityTypes();
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

        if (!(obj instanceof SingleOperandFunction1)) {
            return false;
        }
        
        final SingleOperandFunction1<T> other = (SingleOperandFunction1<T>) obj;
        
        return Objects.equals(operand, other.operand);
    }
}
