package ua.com.fielden.platform.eql.s1.elements;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.s2.elements.CompoundSingleOperand2;
import ua.com.fielden.platform.eql.s2.elements.Expression2;
import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;

public class Expression1 implements ISingleOperand1<Expression2> {

    private final ISingleOperand1<? extends ISingleOperand2> first;

    private final List<CompoundSingleOperand1> items;

    public Expression1(final ISingleOperand1<? extends ISingleOperand2> first, final List<CompoundSingleOperand1> items) {
        super();
        this.first = first;
        this.items = items;
    }

    @Override
    public Expression2 transform(final TransformatorToS2 resolver) {
        final List<CompoundSingleOperand2> transformed = new ArrayList<>();
        for (final CompoundSingleOperand1 item : items) {
            transformed.add(new CompoundSingleOperand2(item.getOperand().transform(resolver), item.getOperator()));
        }
        return new Expression2(first.transform(resolver), transformed);
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
        if (!(obj instanceof Expression1)) {
            return false;
        }
        final Expression1 other = (Expression1) obj;
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
}