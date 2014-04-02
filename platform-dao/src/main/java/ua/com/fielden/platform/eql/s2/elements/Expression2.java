package ua.com.fielden.platform.eql.s2.elements;

import java.math.BigDecimal;
import java.util.List;

public class Expression2 implements ISingleOperand2 {

    private final ISingleOperand2 first;
    private final List<CompoundSingleOperand2> items;

    public Expression2(final ISingleOperand2 first, final List<CompoundSingleOperand2> items) {
        super();
        this.first = first;
        this.items = items;
    }

    @Override
    public boolean ignore() {
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((first == null) ? 0 : first.hashCode());
        result = prime * result + ((items == null) ? 0 : items.hashCode());
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
        if (!(obj instanceof Expression2)) {
            return false;
        }
        final Expression2 other = (Expression2) obj;
        if (first == null) {
            if (other.first != null) {
                return false;
            }
        } else if (!first.equals(other.first)) {
            return false;
        }
        if (items == null) {
            if (other.items != null) {
                return false;
            }
        } else if (!items.equals(other.items)) {
            return false;
        }
        return true;
    }

    @Override
    public Class type() {
        // TODO EQL
        return BigDecimal.class;
    }
}