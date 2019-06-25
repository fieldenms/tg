package ua.com.fielden.platform.eql.stage3.elements.operands;

import static java.util.Collections.emptyList;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Expression3 implements ISingleOperand3 {

    public final ISingleOperand3 first;
    private final List<CompoundSingleOperand3> items;

    public Expression3(final ISingleOperand3 first, final List<CompoundSingleOperand3> items) {
        this.first = first;
        this.items = items;
    }

    public Expression3(final ISingleOperand3 first) {
        this(first, emptyList());
    }
    
    @Override
    public String sql() {
        return items.isEmpty() ? first.sql() : "(" + first.sql() + items.stream().map(co -> co.sql()).collect(Collectors.joining()) +")";
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

        if (!(obj instanceof Expression3)) {
            return false;
        }
        
        final Expression3 other = (Expression3) obj;
        
        return Objects.equals(first, other.first) &&
                Objects.equals(items, other.items);
    }
}