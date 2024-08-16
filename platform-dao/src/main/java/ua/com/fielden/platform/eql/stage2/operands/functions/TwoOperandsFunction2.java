package ua.com.fielden.platform.eql.stage2.operands.functions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static ua.com.fielden.platform.utils.CollectionUtil.concat;

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
        return concat(HashSet::new, operand1.collectEntityTypes(), operand2.collectEntityTypes());
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
