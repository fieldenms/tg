package ua.com.fielden.platform.eql.stage1.operands.functions;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;

abstract class TwoOperandsFunction1<T extends ISingleOperand2<?>> extends AbstractFunction1<T> {
    public final ISingleOperand1<? extends ISingleOperand2<?>> operand1;
    public final ISingleOperand1<? extends ISingleOperand2<?>> operand2;

    public TwoOperandsFunction1(final ISingleOperand1<? extends ISingleOperand2<?>> operand1, final ISingleOperand1<? extends ISingleOperand2<?>> operand2) {
        this.operand1 = operand1;
        this.operand2 = operand2;
    }
    
    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        final Set<Class<? extends AbstractEntity<?>>> result = new HashSet<>();
        result.addAll(operand1.collectEntityTypes());
        result.addAll(operand2.collectEntityTypes());
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + operand1.hashCode();
        result = prime * result + operand2.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof TwoOperandsFunction1)) {
            return false;
        }
        
        final TwoOperandsFunction1<T> other = (TwoOperandsFunction1<T>) obj;
        
        return Objects.equals(operand1, other.operand1) && Objects.equals(operand2, other.operand2);
    }
}