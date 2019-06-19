package ua.com.fielden.platform.eql.stage2.elements;

import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;

public class Yield2 {
    public final ISingleOperand2 operand;
    public final String alias;
    public final boolean hasRequiredHint;
    private ResultQueryYieldDetails2 info;

    public Yield2(final ISingleOperand2 operand, final String alias, final boolean hasRequiredHint) {
        this.operand = operand;
        this.alias = alias;
        this.hasRequiredHint = hasRequiredHint;
    }

    public Yield2(final ISingleOperand2 operand, final String alias) {
        this(operand, alias, false);
    }

    public Class javaType() {
        return operand.type();
    }

    @Override
    public String toString() {
        return alias;//sql();
    }

    public boolean isCompositePropertyHeader() {
        return info != null && info.isCompositeProperty();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((alias == null) ? 0 : alias.hashCode());
        result = prime * result + ((operand == null) ? 0 : operand.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Yield2)) {
            return false;
        }
        final Yield2 other = (Yield2) obj;
        if (alias == null) {
            if (other.alias != null) {
                return false;
            }
        } else if (!alias.equals(other.alias)) {
            return false;
        }
        if (operand == null) {
            if (other.operand != null) {
                return false;
            }
        } else if (!operand.equals(other.operand)) {
            return false;
        }
        return true;
    }
}