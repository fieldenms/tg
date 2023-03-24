package ua.com.fielden.platform.eql.stage3.operands;

import java.util.Objects;

public abstract class AbstractSingleOperand3 implements ISingleOperand3 {
    public final Class<?> type;
    
    public AbstractSingleOperand3(final Class<?> type) {
        this.type = type;
    }
    
    public Class<?> type() {
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