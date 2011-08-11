package ua.com.fielden.platform.types;

import javax.swing.SortOrder;

import ua.com.fielden.platform.reportquery.IDistributedProperty;

public class Ordering<TOKEN, PROPERTY extends IDistributedProperty> {

    private final PROPERTY property;
    private final TOKEN token;
    private final SortOrder sortOrder;

    public Ordering(final PROPERTY aggregationProperty, final TOKEN aggregationFunction, final SortOrder sortOrder) {
	this.property = aggregationProperty;
	this.token = aggregationFunction;
	this.sortOrder = sortOrder;
    }

    public TOKEN getToken() {
	return token;
    }

    public PROPERTY getProperty() {
	return property;
    }

    public SortOrder getSortOrder() {
	return sortOrder;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null || obj.getClass() != this.getClass()) {
	    return false;
	}
	final Ordering<TOKEN, PROPERTY> ordering = (Ordering<TOKEN, PROPERTY>) obj;
	if ((getProperty() == null && getProperty() != ordering.getProperty()) || (getProperty() != null && !getProperty().equals(ordering.getProperty()))) {
	    return false;
	}
	if ((getToken() == null && getToken() != ordering.getToken()) || (getToken() != null && !getToken().equals(ordering.getToken()))) {
	    return false;
	}
	if ((getSortOrder() == null && getSortOrder() != ordering.getSortOrder()) || (getSortOrder() != null && !getSortOrder().equals(ordering.getSortOrder()))) {
	    return false;
	}
	return true;
    }

    @Override
    public String toString() {
	return getProperty().toString() + " " + getToken() + " " + getSortOrder().toString();
    }

    @Override
    public int hashCode() {
	int result = 17;
	result = 31 * result + (getProperty() != null ? getProperty().hashCode() : 0);
	result = 31 * result + (getToken() != null ? getToken().hashCode() : 0);
	result = 31 * result + (getSortOrder() != null ? getSortOrder().hashCode() : 0);
	return result;
    }

}
