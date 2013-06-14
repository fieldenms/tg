package ua.com.fielden.platform.eql.s2.elements;

import java.util.ArrayList;
import java.util.List;

public class OrderBys2 implements IElement2 {
    private final List<OrderBy2> models;

    public OrderBys2(final List<OrderBy2> models) {
	this.models = models;
    }

    @Override
    public String toString() {
	return models.toString();
    }

    @Override
    public List<EntValue2> getAllValues() {
	final List<EntValue2> result = new ArrayList<EntValue2>();
	for (final OrderBy2 model : models) {
	    if (model.getOperand() != null) {
		result.addAll(model.getOperand().getAllValues());
	    }
	}
	return result;
    }

    public List<OrderBy2> getModels() {
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
	if (!(obj instanceof OrderBys2)) {
	    return false;
	}
	final OrderBys2 other = (OrderBys2) obj;
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