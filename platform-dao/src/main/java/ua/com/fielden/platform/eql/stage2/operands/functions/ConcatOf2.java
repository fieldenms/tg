package ua.com.fielden.platform.eql.stage2.operands.functions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.functions.ConcatOf3;
import ua.com.fielden.platform.eql.stage3.operands.functions.ConcatOfOrderItem3;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public class ConcatOf2 extends TwoOperandsFunction2<ConcatOf3> {

    public final List<ConcatOfOrderItem2> orderItems;

    public ConcatOf2(
            final ISingleOperand2<? extends ISingleOperand3> operand1,
            final ISingleOperand2<? extends ISingleOperand3> operand2,
            final List<ConcatOfOrderItem2> orderItems)
    {
        super(operand1, operand2, operand1.type());
        this.orderItems = List.copyOf(orderItems);
    }

    public ConcatOf2(
            final ISingleOperand2<? extends ISingleOperand3> operand1,
            final ISingleOperand2<? extends ISingleOperand3> operand2)
    {
        this(operand1, operand2, List.of());
    }

    @Override
    public Set<Prop2> collectProps() {
        return Stream.concat(super.collectProps().stream(),
                             orderItems.stream().map(ConcatOfOrderItem2::collectProps).flatMap(Collection::stream))
                .collect(toSet());
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return Stream.concat(super.collectEntityTypes().stream(),
                             orderItems.stream().map(ConcatOfOrderItem2::collectEntityTypes).flatMap(Collection::stream))
                .collect(toSet());
    }

    @Override
    public TransformationResultFromStage2To3<ConcatOf3> transform(final TransformationContextFromStage2To3 context) {
        final var firstTr = operand1.transform(context);
        final var secondTr = operand2.transform(firstTr.updatedContext);

        var ctx = secondTr.updatedContext;
        final var orderItems3 = new ArrayList<ConcatOfOrderItem3>(orderItems.size());
        for (final var item : orderItems) {
            final var itemTr = item.transform(ctx);
            orderItems3.add(itemTr.item);
            ctx = itemTr.updatedContext;
        }

        return new TransformationResultFromStage2To3<>(
                new ConcatOf3(firstTr.item, secondTr.item, type, List.copyOf(orderItems3)),
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
