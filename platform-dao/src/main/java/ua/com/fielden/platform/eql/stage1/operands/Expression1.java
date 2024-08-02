package ua.com.fielden.platform.eql.stage1.operands;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage2.operands.Expression2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.utils.StreamUtils.concat;

public class Expression1 implements ISingleOperand1<Expression2> {

    public final ISingleOperand1<? extends ISingleOperand2<?>> first;
    private final List<CompoundSingleOperand1> items;

    public Expression1(final ISingleOperand1<? extends ISingleOperand2<?>> first, final List<CompoundSingleOperand1> items) {
        this.first = first;
        this.items = items;
    }

    @Override
    public Expression2 transform(final TransformationContextFromStage1To2 context) {
        return items.isEmpty() ? new Expression2(first.transform(context), emptyList()) : new Expression2(first.transform(context), items.stream().map(el -> el.transform(context)).collect(toList()));
    }
    
    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return concat(
                items.stream().map(el -> el.operand.collectEntityTypes()).flatMap(Set::stream),
                first.collectEntityTypes().stream())
                .collect(toSet());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + first.hashCode();
        result = prime * result + items.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Expression1)) {
            return false;
        }
        final Expression1 other = (Expression1) obj;

        return Objects.equals(first, other.first) && Objects.equals(items, other.items);
    }
}
