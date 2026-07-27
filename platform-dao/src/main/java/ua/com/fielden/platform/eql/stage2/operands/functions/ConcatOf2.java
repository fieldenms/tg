package ua.com.fielden.platform.eql.stage2.operands.functions;

import com.google.common.collect.ImmutableList;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.sundries.OrderBy2;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

import static ua.com.fielden.platform.eql.meta.PropType.STRING_PROP_TYPE;
import ua.com.fielden.platform.eql.stage3.operands.functions.ConcatOf3;
import ua.com.fielden.platform.eql.stage3.sundries.OrderBy3;
import ua.com.fielden.platform.eql.stage3.sundries.Yields3;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public class ConcatOf2 extends TwoOperandsFunction2<ConcatOf3> {

    public final List<OrderBy2> orderItems;

    public ConcatOf2(
            final ISingleOperand2<? extends ISingleOperand3> operand1,
            final ISingleOperand2<? extends ISingleOperand3> operand2,
            final List<OrderBy2> orderItems)
    {
        super(operand1, operand2, STRING_PROP_TYPE);
        this.orderItems = ImmutableList.copyOf(orderItems);
    }

    public ConcatOf2(
            final ISingleOperand2<? extends ISingleOperand3> operand1,
            final ISingleOperand2<? extends ISingleOperand3> operand2)
    {
        this(operand1, operand2, ImmutableList.of());
    }

    @Override
    public Set<Prop2> collectProps() {
        return Stream.concat(super.collectProps().stream(),
                             orderItems.stream().map(OrderBy2::collectProps).flatMap(Collection::stream))
                .collect(toSet());
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return Stream.concat(super.collectEntityTypes().stream(),
                             orderItems.stream().map(OrderBy2::collectEntityTypes).flatMap(Collection::stream))
                .collect(toSet());
    }

    @Override
    public TransformationResultFromStage2To3<ConcatOf3> transform(final TransformationContextFromStage2To3 context) {
        final var firstTr = operand1.transform(context);
        final var secondTr = operand2.transform(firstTr.updatedContext);

        var ctx = secondTr.updatedContext;
        final var orderItems3 = ImmutableList.<OrderBy3>builderWithExpectedSize(orderItems.size());
        for (final var item : orderItems) {
            final var itemTr = item.transform(ctx, Yields3.EMPTY);
            orderItems3.add(itemTr.item);
            ctx = itemTr.updatedContext;
        }

        return new TransformationResultFromStage2To3<>(
                new ConcatOf3(firstTr.item, secondTr.item, type, orderItems3.build()),
                ctx);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ConcatOf2.class.getName().hashCode();
        result = prime * result + orderItems.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
               || obj instanceof ConcatOf2 that
                  && super.equals(that)
                  && Objects.equals(orderItems, that.orderItems);
    }

}
