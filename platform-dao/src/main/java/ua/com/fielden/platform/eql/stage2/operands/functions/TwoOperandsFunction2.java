package ua.com.fielden.platform.eql.stage2.operands.functions;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

abstract class TwoOperandsFunction2<T extends ISingleOperand3> extends AbstractFunction2<T> {
    public final ISingleOperand2<? extends ISingleOperand3> operand1;
    public final ISingleOperand2<? extends ISingleOperand3> operand2;

    public TwoOperandsFunction2(final ISingleOperand2<? extends ISingleOperand3> operand1, final ISingleOperand2<? extends ISingleOperand3> operand2, final PropType type) {
        super(type);
        this.operand1 = operand1;
        this.operand2 = operand2;
    }

    @Override
    public Set<Prop2> collectProps() {
        final Set<Prop2> result = new HashSet<>();
        result.addAll(operand1.collectProps());
        result.addAll(operand2.collectProps());
        return result;
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

        if (!(obj instanceof TwoOperandsFunction2)) {
            return false;
        }
        
        final TwoOperandsFunction2<?> other = (TwoOperandsFunction2<?>) obj;
        
        return Objects.equals(operand1, other.operand1) && Objects.equals(operand2, other.operand2);
    }
}