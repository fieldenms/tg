package ua.com.fielden.platform.eql.stage2.elements.operands;

import static java.util.Collections.emptyList;

import java.math.BigDecimal;
import java.util.List;

import com.google.common.base.Objects;

public class Expression2 implements ISingleOperand2 {

    public final ISingleOperand2 first;
    private final List<CompoundSingleOperand2> items;

    public Expression2(final ISingleOperand2 first, final List<CompoundSingleOperand2> items) {
        this.first = first;
        this.items = items;
    }

    public Expression2(final ISingleOperand2 first) {
        this(first, emptyList());
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

        if (!(obj instanceof Expression2)) {
            return false;
        }
        
        final Expression2 other = (Expression2) obj;
        
        return Objects.equal(first, other.first) &&
                Objects.equal(items, other.items);
    }

    @Override
    public Class type() {
        // TODO EQL
        return BigDecimal.class;
    }
}