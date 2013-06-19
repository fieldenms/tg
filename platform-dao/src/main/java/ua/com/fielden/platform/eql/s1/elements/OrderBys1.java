package ua.com.fielden.platform.eql.s1.elements;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.s2.elements.OrderBy2;
import ua.com.fielden.platform.eql.s2.elements.OrderBys2;

public class OrderBys1 implements IElement1<OrderBys2> {
    private final List<OrderBy1> models;

    public OrderBys1(final List<OrderBy1> models) {
	this.models = models;
    }

    @Override
    public OrderBys2 transform(final TransformatorToS2 resolver) {
	final List<OrderBy2> transformed = new ArrayList<>();
	for (final OrderBy1 orderBy : models) {
	    transformed.add(new OrderBy2(orderBy.getOperand().transform(resolver), orderBy.isDesc()));
	}
	return new OrderBys2(transformed);
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