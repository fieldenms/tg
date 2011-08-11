package ua.com.fielden.platform.equery.tokens.properties;

public final class GroupByProperty extends AbstractQueryProperty {

    /**
     * This constructor is required mainly for serialisation.
     * @param expression
     */
    public GroupByProperty() {
    }

    public GroupByProperty(final String rawValue, final Object...values) {
	super(rawValue, values);
    }

    private GroupByProperty(final GroupByProperty original) {
	super(original);
    }

    public GroupByProperty clon() {
	return new GroupByProperty(this);
    }

    @Override
    PropertyOrigin getPropertyOrigin() {
	return PropertyOrigin.GROUPBY;
    }
}
