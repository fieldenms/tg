package ua.com.fielden.platform.eql.stage1.operands.functions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.utils.ToString;

import java.util.Objects;
import java.util.Set;

public abstract class SingleOperandFunction1<T extends ISingleOperand2<?>> implements IFunction1<T>, ToString.IFormattable {

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
        return this == obj || obj instanceof SingleOperandFunction1 that && Objects.equals(operand, that.operand);
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines);
    }

    protected ToString addToString(final ToString toString) {
        return toString;
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this).add("operand", operand).pipe(this::addToString).$();
    }

}
