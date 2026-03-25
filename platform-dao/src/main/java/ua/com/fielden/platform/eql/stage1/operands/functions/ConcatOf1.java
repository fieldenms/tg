package ua.com.fielden.platform.eql.stage1.operands.functions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage1.operands.Value1;
import ua.com.fielden.platform.eql.stage1.sundries.OrderBy1;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.functions.ConcatOf2;
import ua.com.fielden.platform.eql.stage2.sundries.OrderBy2;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static ua.com.fielden.platform.entity.exceptions.InvalidArgumentException.requireNotNullArgument;

public class ConcatOf1 extends TwoOperandsFunction1<ConcatOf2> {

    public final List<OrderBy1> orderItems;

    public ConcatOf1(
            final ISingleOperand1<? extends ISingleOperand2<?>> expr,
            final Value1 separator,
            final List<OrderBy1> orderItems)
    {
        super(expr, separator);
        requireNotNullArgument(separator, "separator");
        requireNotNullArgument(orderItems, "orderItems");
        this.orderItems = List.copyOf(orderItems);
    }

    public ConcatOf1(
            final ISingleOperand1<? extends ISingleOperand2<?>> expr,
            final Value1 separator)
    {
        this(expr, separator, List.of());
    }

    @Override
    public ConcatOf2 transform(final TransformationContextFromStage1To2 context) {
        final List<OrderBy2> orderItems2 = orderItems.stream()
                .map(item -> item.transform(context))
                .toList();
        return new ConcatOf2(operand1.transform(context), operand2.transform(context), orderItems2);
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        final var result = new HashSet<>(super.collectEntityTypes());
        for (final var item : orderItems) {
            result.addAll(item.operand().collectEntityTypes());
        }
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ConcatOf1.class.getName().hashCode();
        result = prime * result + orderItems.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
               || obj instanceof ConcatOf1 that
                  && super.equals(that)
                  && Objects.equals(orderItems, that.orderItems);
    }

}
