package ua.com.fielden.platform.equery.tokens.properties;

public final class SearchProperty extends AbstractQueryProperty {

    /**
     * This constructor is required mainly for serialisation.
     * @param expression
     */
    public SearchProperty() {
    }

    public SearchProperty(final String rawValue, final Object... values) {
	super(rawValue, values);
    }

    private SearchProperty(final SearchProperty original) {
	super(original);
    }

    public SearchProperty clon() {
	return new SearchProperty(this);
    }

    @Override
    PropertyOrigin getPropertyOrigin() {
	return PropertyOrigin.WHERE;
    }
}