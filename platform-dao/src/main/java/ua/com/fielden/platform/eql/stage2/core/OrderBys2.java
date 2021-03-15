package ua.com.fielden.platform.eql.stage2.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.eql.stage2.TransformationContext;
import ua.com.fielden.platform.eql.stage2.TransformationResult;
import ua.com.fielden.platform.eql.stage2.operands.EntProp2;
import ua.com.fielden.platform.eql.stage3.core.OrderBy3;
import ua.com.fielden.platform.eql.stage3.core.OrderBys3;
import ua.com.fielden.platform.eql.stage3.core.Yields3;

public class OrderBys2 {
    private final List<OrderBy2> models;

    public OrderBys2(final List<OrderBy2> models) {
        this.models = models;
    }

    public TransformationResult<OrderBys3> transform(final TransformationContext context, final Yields3 yields) {
        final List<OrderBy3> transformed = new ArrayList<>();
        TransformationContext currentContext = context;
        for (final OrderBy2 orderBy : models) {
            final TransformationResult<OrderBy3> orderByTr = orderBy.transform(currentContext, yields);
            transformed.add(orderByTr.item);
            currentContext = orderByTr.updatedContext;
        }
        return new TransformationResult<OrderBys3>(new OrderBys3(transformed), currentContext);
    }

    public Set<EntProp2> collectProps() {
        final Set<EntProp2> result = new HashSet<>();
        for (final OrderBy2 orderBy : models) {
            if (orderBy.operand != null) {
                result.addAll(orderBy.operand.collectProps());
            }
        }
        return result;
    }

    public List<OrderBy2> getModels() {
        return Collections.unmodifiableList(models);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + models.hashCode();
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

        return Objects.equals(models, other.models);
    }
}