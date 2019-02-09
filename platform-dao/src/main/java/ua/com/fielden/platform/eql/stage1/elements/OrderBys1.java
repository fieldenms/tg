package ua.com.fielden.platform.eql.stage1.elements;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.eql.meta.PropsResolutionContext;
import ua.com.fielden.platform.eql.meta.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.OrderBy2;
import ua.com.fielden.platform.eql.stage2.elements.OrderBys2;

public class OrderBys1 {
    private final List<OrderBy1> models;

    public OrderBys1(final List<OrderBy1> models) {
        this.models = models;
    }

    public TransformationResult<OrderBys2> transform(final PropsResolutionContext resolutionContext) {
        final List<OrderBy2> transformed = new ArrayList<>();
        PropsResolutionContext currentResolutionContext = resolutionContext;
        for (final OrderBy1 orderBy : models) {
            TransformationResult<OrderBy2> orderByTransformationResult = orderBy.transform(currentResolutionContext);
            transformed.add(orderByTransformationResult.getItem());
            currentResolutionContext = orderByTransformationResult.getUpdatedContext();
        }
        return new TransformationResult<OrderBys2>(new OrderBys2(transformed), currentResolutionContext);
    }

    @Override
    public String toString() {
        return models.toString();
    }

    public List<OrderBy1> getModels() {
        return models;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((models == null) ? 0 : models.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof OrderBys1)) {
            return false;
        }
        final OrderBys1 other = (OrderBys1) obj;
        if (models == null) {
            if (other.models != null) {
                return false;
            }
        } else if (!models.equals(other.models)) {
            return false;
        }
        return true;
    }
}