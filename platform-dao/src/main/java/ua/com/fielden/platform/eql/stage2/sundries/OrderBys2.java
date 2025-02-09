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

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static ua.com.fielden.platform.eql.stage1.sundries.OrderBys1.NO_OFFSET;

/**
 * To identify empty order-bys with no limit and no offset, {@link #EMPTY_ORDER_BYS} can be compared with {@code ==}
 * (due to only static methods available for creating instances).
 */
public class OrderBys2 {
    public static final OrderBys2 EMPTY_ORDER_BYS = new OrderBys2(ImmutableList.of(), Limit.all(), NO_OFFSET);
    
    private final List<OrderBy2> orderBys;
    private final Limit limit;
    private final long offset;

    public static OrderBys2 orderBys2(final List<OrderBy2> orderBys, final Limit limit, final long offset) {
        if (orderBys.isEmpty() && limit instanceof Limit.All && offset == NO_OFFSET) {
            return EMPTY_ORDER_BYS;
        }
        return new OrderBys2(orderBys, limit, offset);
    }

    public static OrderBys2 orderBys2(final List<OrderBy2> orderBys) {
        return orderBys.isEmpty() ? EMPTY_ORDER_BYS : new OrderBys2(orderBys, Limit.all(), NO_OFFSET);
    }

    private OrderBys2(final List<OrderBy2> orderBys, final Limit limit, final long offset) {
        this.orderBys = ImmutableList.copyOf(orderBys);
        this.limit = limit;
        this.offset = offset;
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
                .map(o -> o.operand)
                .filter(Objects::nonNull)
                .map(ITransformableFromStage2To3::collectProps)
                .flatMap(Set::stream)
                .collect(toImmutableSet());
    }
    
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return orderBys.isEmpty()
                ? ImmutableSet.of()
                : orderBys.stream()
                        .map(o -> o.operand)
                        .filter(Objects::nonNull)
                        .map(ITransformableFromStage2To3::collectEntityTypes)
                        .flatMap(Set::stream)
                        .collect(toImmutableSet());
    }
    
    public List<OrderBy2> getOrderBys() {
        return orderBys;
    }

    public long offset() {
        return offset;
    }

    public Limit limit() {
        return limit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderBys, limit, offset);
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof OrderBys2 that
                              && offset == that.offset
                              && limit.equals(that.limit)
                              && orderBys.equals(that.orderBys);
    }

}
