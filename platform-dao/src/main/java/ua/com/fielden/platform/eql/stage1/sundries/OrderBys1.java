package ua.com.fielden.platform.eql.stage1.sundries;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.Limit;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage2.sundries.OrderBys2;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.eql.stage2.sundries.OrderBys2.orderBys2;

/**
 * To identify empty order-bys with no limit and no offset, {@link #EMPTY_ORDER_BYS} can be compared with {@code ==}
 * (due to only static methods available for creating instances).
 */
public class OrderBys1 {
    public static final long NO_OFFSET = 0;
    public static final OrderBys1 EMPTY_ORDER_BYS = new OrderBys1(emptyList(), Limit.all(), NO_OFFSET);

    private final List<OrderBy1> models;
    private final Limit limit;
    private final long offset;

    public static OrderBys1 orderBys1(final List<OrderBy1> models, final Limit limit, final long offset) {
        if (models.isEmpty() && limit instanceof Limit.All && offset == NO_OFFSET) {
            return EMPTY_ORDER_BYS;
        }
        return new OrderBys1(models, limit, offset);
    }

    public static OrderBys1 orderBys1(final List<OrderBy1> models) {
        return models.isEmpty() ? EMPTY_ORDER_BYS : new OrderBys1(models, Limit.all(), NO_OFFSET);
    }

    private OrderBys1(final List<OrderBy1> models, final Limit limit, final long offset) {
        this.models = models;
        this.limit = limit;
        this.offset = offset;
    }

    public OrderBys2 transform(final TransformationContextFromStage1To2 context) {
        if (this == EMPTY_ORDER_BYS) {
            return OrderBys2.EMPTY_ORDER_BYS;
        } else {
            return orderBys2(models.stream().map(el -> el.transform(context)).collect(toList()), limit, offset);
        }
    }

    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return models.isEmpty() ? emptySet() : models.stream().filter(el -> el.operand != null).map(el -> el.operand.collectEntityTypes()).flatMap(Set::stream).collect(toSet());
    }

    public Stream<OrderBy1> models() {
        return models.stream();
    }

    public Limit limit() {
        return limit;
    }

    public long offset() {
        return offset;
    }

    public boolean isEmpty() {
        return models.isEmpty();
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(models, limit, offset);
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof OrderBys1 that
                              && offset == that.offset
                              && limit.equals(that.limit)
                              && models.equals(that.models);
    }

}
