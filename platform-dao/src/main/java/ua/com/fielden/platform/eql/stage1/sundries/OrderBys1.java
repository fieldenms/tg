package ua.com.fielden.platform.eql.stage1.sundries;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.Limit;
import ua.com.fielden.platform.eql.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage2.sundries.OrderBys2;
import ua.com.fielden.platform.utils.ToString;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static ua.com.fielden.platform.eql.stage2.sundries.OrderBys2.orderBys2;

/**
 * To identify empty order-bys with no limit and no offset, {@link #EMPTY_ORDER_BYS} can be compared with {@code ==}
 * (due to only static methods available for creating instances).
 * <p>
 * Prefer static methods for instantiation over the constructor.
 */
public record OrderBys1 (List<OrderBy1> models, Limit limit, long offset) implements ToString.IFormattable {

    public static final long NO_OFFSET = 0;
    public static final OrderBys1 EMPTY_ORDER_BYS = new OrderBys1(ImmutableList.of(), Limit.all(), NO_OFFSET);

    public static OrderBys1 orderBys1(final List<OrderBy1> models, final Limit limit, final long offset) {
        if (models.isEmpty() && limit instanceof Limit.All && offset == NO_OFFSET) {
            return EMPTY_ORDER_BYS;
        }
        return new OrderBys1(models, limit, offset);
    }

    public static OrderBys1 orderBys1(final List<OrderBy1> models) {
        return models.isEmpty() ? EMPTY_ORDER_BYS : new OrderBys1(models, Limit.all(), NO_OFFSET);
    }

    public OrderBys1(final List<OrderBy1> models, final Limit limit, final long offset) {
        if (limit instanceof Limit.Count (var n) && n < 0) {
            throw new EqlStage1ProcessingException("Limit must be a non-negative integer, but was: %s".formatted(n));
        }
        if (offset < 0) {
            throw new EqlStage1ProcessingException("Offset must be a non-negative integer, but was: %s".formatted(offset));
        }
        this.models = ImmutableList.copyOf(models);
        this.limit = limit;
        this.offset = offset;
    }

    public OrderBys2 transform(final TransformationContextFromStage1To2 context) {
        if (this == EMPTY_ORDER_BYS) {
            return OrderBys2.EMPTY_ORDER_BYS;
        } else {
            return orderBys2(models.stream().map(el -> el.transform(context)).collect(toImmutableList()), limit, offset);
        }
    }

    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return models.isEmpty()
                ? ImmutableSet.of()
                : models.stream()
                        .filter(el -> el.operand() != null)
                        .map(el -> el.operand().collectEntityTypes()).flatMap(Set::stream)
                        .collect(toImmutableSet());
    }

    public boolean isEmpty() {
        return models.isEmpty();
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("models", models)
                .add("limit", limit)
                .add("offset", offset)
                .$();
    }

}
