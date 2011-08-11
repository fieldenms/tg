package ua.com.fielden.platform.equery.tokens.properties;


public final class SelectCalculatedProperty extends AbstractQueryProperty {
    private String propertyAlias;

    /**
     * This constructor is required mainly for serialisation.
     * @param expression
     */
    protected SelectCalculatedProperty() {
    }

    public SelectCalculatedProperty(final String rawValue, final String alias, final Object...values) {
	super(rawValue, values);
	this.propertyAlias = alias;
    }

    private SelectCalculatedProperty(final SelectCalculatedProperty original) {
	super(original);
	this.propertyAlias = original.getPropertyAlias();
    }

    public SelectCalculatedProperty clon() {
	return new SelectCalculatedProperty(this);
    }

    @Override
    PropertyOrigin getPropertyOrigin() {
	return PropertyOrigin.SELECT;
    }

    public String getPropertyAlias() {
	return propertyAlias;
    }

    @Override
    public String toString() {
	return super.toString() + getPropertyAlias();
    }
}
