package ua.com.fielden.platform.eql.stage3.operands;

import java.util.Objects;

import ua.com.fielden.platform.eql.meta.PropType;

public abstract class AbstractSingleOperand3 implements ISingleOperand3 {
    public final PropType type;
    
    public AbstractSingleOperand3(final PropType type) {
        this.type = type;
    }
    
    @Override
    public PropType type() {
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + type.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AbstractSingleOperand3)) {
            return false;
        }

        final AbstractSingleOperand3 other = (AbstractSingleOperand3) obj;

        return Objects.equals(type, other.type);
    }
}