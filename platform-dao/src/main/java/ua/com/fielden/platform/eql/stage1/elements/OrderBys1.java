package ua.com.fielden.platform.eql.stage1.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.eql.stage2.elements.OrderBys2;
import ua.com.fielden.platform.eql.stage2.elements.sources.IQrySource2;
import ua.com.fielden.platform.eql.stage3.elements.sources.IQrySource3;

public class OrderBys1 {
    private final List<OrderBy1> models;

    public OrderBys1(final List<OrderBy1> models) {
        this.models = models;
    }

    public OrderBys2 transform(final PropsResolutionContext context, final Yields1 yields1, final IQrySource2<? extends IQrySource3> mainSource) {
        return new OrderBys2(models.stream().map(el -> el.transform(context, yields1, mainSource)).collect(ArrayList::new, List::addAll, List::addAll));
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