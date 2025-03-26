package ua.com.fielden.platform.eql.stage2.sundries;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.Limit;
import ua.com.fielden.platform.eql.stage2.ITransformableFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage3.sundries.OrderBy3;
import ua.com.fielden.platform.eql.stage3.sundries.OrderBys3;
import ua.com.fielden.platform.eql.stage3.sundries.Yields3;
import ua.com.fielden.platform.utils.ToString;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static ua.com.fielden.platform.eql.stage1.sundries.OrderBys1.NO_OFFSET;

/**
 * To identify empty order-bys with no limit and no offset, {@link #EMPTY_ORDER_BYS} can be compared with {@code ==}
 * (due to only static methods available for creating instances).
 * <p>
 * Prefer static method {@link #orderBys()} over the constructor.
 */
public record OrderBys2 (List<OrderBy2> orderBys, Limit limit, long offset) implements ToString.IFormattable {

    public static final OrderBys2 EMPTY_ORDER_BYS = new OrderBys2(ImmutableList.of(), Limit.all(), NO_OFFSET);
    
    public static OrderBys2 orderBys2(final List<OrderBy2> orderBys, final Limit limit, final long offset) {
        if (orderBys.isEmpty() && limit instanceof Limit.All && offset == NO_OFFSET) {
            return EMPTY_ORDER_BYS;
        }
        return new OrderBys2(orderBys, limit, offset);
    }

    public static OrderBys2 orderBys2(final List<OrderBy2> orderBys) {
        return orderBys.isEmpty() ? EMPTY_ORDER_BYS : new OrderBys2(orderBys, Limit.all(), NO_OFFSET);
    }

    public OrderBys2(final List<OrderBy2> orderBys, final Limit limit, final long offset) {
        this.orderBys = ImmutableList.copyOf(orderBys);
        this.limit = limit;
        this.offset = offset;
    }

    public boolean isEmpty() {
        return orderBys.isEmpty();
    }

    public OrderBys2 updateOrderBys(final List<OrderBy2> newOrderBys) {
        return new OrderBys2(newOrderBys, limit, offset);
    }

    public TransformationResultFromStage2To3<OrderBys3> transform(final TransformationContextFromStage2To3 context, final Yields3 yields) {
        if (this == EMPTY_ORDER_BYS) {
            return new TransformationResultFromStage2To3<>(null, context);
        }
        
        final var transformed = ImmutableList.<OrderBy3>builder();
        TransformationContextFromStage2To3 currentContext = context;
        for (final OrderBy2 orderBy : orderBys) {
            final TransformationResultFromStage2To3<OrderBy3> orderByTr = orderBy.transform(currentContext, yields);
            transformed.add(orderByTr.item);
            currentContext = orderByTr.updatedContext;
        }
        return new TransformationResultFromStage2To3<>(new OrderBys3(transformed.build(), limit, offset), currentContext);
    }

    public Set<Prop2> collectProps() {
        return orderBys.stream()
                .map(OrderBy2::operand)
                .filter(Objects::nonNull)
                .map(ITransformableFromStage2To3::collectProps)
                .flatMap(Set::stream)
                .collect(toImmutableSet());
    }
    
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return orderBys.isEmpty()
                ? ImmutableSet.of()
                : orderBys.stream()
                        .map(OrderBy2::operand)
                        .filter(Objects::nonNull)
                        .map(ITransformableFromStage2To3::collectEntityTypes)
                        .flatMap(Set::stream)
                        .collect(toImmutableSet());
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines);
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("limit", limit)
                .add("offset", offset)
                .add("orderBys", orderBys)
                .$();
    }

    public OrderBys2 setModels(final List<OrderBy2> models) {
        return orderBys2(models, limit, offset);
    }

}
