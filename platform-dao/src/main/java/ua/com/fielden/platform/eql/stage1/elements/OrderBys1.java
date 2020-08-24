package ua.com.fielden.platform.eql.stage1.elements;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.eql.stage2.elements.OrderBys2;

public class OrderBys1 {
    private final List<OrderBy1> models;

    public OrderBys1(final List<OrderBy1> models) {
        this.models = models;
    }

    public OrderBys2 transform(final PropsResolutionContext context) {
        return new OrderBys2(models.stream().map(el -> el.transform(context)).collect(toList()));
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