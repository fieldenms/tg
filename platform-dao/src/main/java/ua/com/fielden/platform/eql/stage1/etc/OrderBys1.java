package ua.com.fielden.platform.eql.stage1.etc;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.eql.stage1.TransformationContext1;
import ua.com.fielden.platform.eql.stage2.etc.OrderBys2;

public class OrderBys1 {
    public static final OrderBys1 emptyOrderBys = new OrderBys1(emptyList());
    
    private final List<OrderBy1> models;

    public OrderBys1(final List<OrderBy1> models) {
        this.models = models;
    }

    public OrderBys2 transform(final TransformationContext1 context) {
        if (models.isEmpty()) {
            return OrderBys2.emptyOrderBys;
        } else {
            return new OrderBys2(models.stream().map(el -> el.transform(context)).collect(toList()));
        }
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

        if (!(obj instanceof OrderBys1)) {
            return false;
        }

        final OrderBys1 other = (OrderBys1) obj;

        return Objects.equals(models, other.models);
    }
}