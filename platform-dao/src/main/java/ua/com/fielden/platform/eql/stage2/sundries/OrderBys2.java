package ua.com.fielden.platform.eql.stage2.sundries;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage3.sundries.OrderBy3;
import ua.com.fielden.platform.eql.stage3.sundries.OrderBys3;
import ua.com.fielden.platform.eql.stage3.sundries.Yields3;

public class OrderBys2 {
    public static final OrderBys2 EMPTY_ORDER_BYS = new OrderBys2(emptyList());
    
    private final List<OrderBy2> orderBys;

    public OrderBys2(final List<OrderBy2> orderBys) {
        this.orderBys = orderBys;
    }

    public TransformationResultFromStage2To3<OrderBys3> transform(final TransformationContextFromStage2To3 context, final Yields3 yields) {
        if (orderBys.isEmpty()) {
            return new TransformationResultFromStage2To3<>(null, context);
        }
        
        final List<OrderBy3> transformed = new ArrayList<>();
        TransformationContextFromStage2To3 currentContext = context;
        for (final OrderBy2 orderBy : orderBys) {
            final TransformationResultFromStage2To3<OrderBy3> orderByTr = orderBy.transform(currentContext, yields);
            transformed.add(orderByTr.item);
            currentContext = orderByTr.updatedContext;
        }
        return new TransformationResultFromStage2To3<>(new OrderBys3(transformed), currentContext);
    }

    public Set<Prop2> collectProps() {
        final Set<Prop2> result = new HashSet<>();
        for (final OrderBy2 orderBy : orderBys) {
            if (orderBy.operand != null) {
                result.addAll(orderBy.operand.collectProps());
            }
        }
        return result;
    }
    
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return orderBys.isEmpty() ? emptySet() : orderBys.stream().filter(el -> el.operand != null).map(el -> el.operand.collectEntityTypes()).flatMap(Set::stream).collect(toSet());
    }
    
    public List<OrderBy2> getOrderBys() {
        return Collections.unmodifiableList(orderBys);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + orderBys.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof OrderBys2)) {
            return false;
        }

        final OrderBys2 other = (OrderBys2) obj;

        return Objects.equals(orderBys, other.orderBys);
    }
}