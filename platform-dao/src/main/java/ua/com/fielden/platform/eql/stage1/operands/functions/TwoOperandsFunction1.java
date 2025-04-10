package ua.com.fielden.platform.eql.stage1.operands.functions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.utils.ToString;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static ua.com.fielden.platform.utils.CollectionUtil.concat;

abstract class TwoOperandsFunction1<T extends ISingleOperand2<?>> implements IFunction1<T>, ToString.IFormattable {
    public final ISingleOperand1<? extends ISingleOperand2<?>> operand1;
    public final ISingleOperand1<? extends ISingleOperand2<?>> operand2;

    public TwoOperandsFunction1(final ISingleOperand1<? extends ISingleOperand2<?>> operand1, final ISingleOperand1<? extends ISingleOperand2<?>> operand2) {
        this.operand1 = operand1;
        this.operand2 = operand2;
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
        return this == obj
               || obj instanceof TwoOperandsFunction1 that
                  && Objects.equals(operand1, that.operand1)
                  && Objects.equals(operand2, that.operand2);
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines());
    }

    protected ToString addToString(final ToString toString) {
        return toString;
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("operand1", operand1)
                .add("operand2", operand2)
                .pipe(this::addToString)
                .$();
    }

}
