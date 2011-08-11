package ua.com.fielden.platform.equery.tokens.properties;

import ua.com.fielden.platform.equery.Ordering;
import ua.com.fielden.platform.equery.RootEntityMapper;

public final class OrderByProperty extends AbstractQueryProperty {
    private final Ordering ordering;

    /**
     * This constructor is required mainly for serialisation.
     * @param expression
     */
    protected OrderByProperty() {
	ordering = Ordering.ASC;
    }

    public OrderByProperty(final String rawValue, final Ordering ordering) {
	super(rawValue.trim());
	this.ordering = ordering;
    }

    private OrderByProperty(final OrderByProperty original) {
	super(original);
	this.ordering = original.getOrdering();
    }

    @Override
    public String getSql(final RootEntityMapper entityMapper) {
	return super.getSql(entityMapper) + " " + ordering;
    }

    public OrderByProperty clon() {
	return new OrderByProperty(this);
    }

    public Ordering getOrdering() {
	return ordering;
    }

    @Override
    PropertyOrigin getPropertyOrigin() {
	return PropertyOrigin.ORDERBY;
    }

    @Override
    public String toString() {
	return super.toString() + ordering;
    }
}