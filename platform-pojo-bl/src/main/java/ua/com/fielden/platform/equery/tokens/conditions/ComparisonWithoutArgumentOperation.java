package ua.com.fielden.platform.equery.tokens.conditions;

public enum ComparisonWithoutArgumentOperation {
    IS_TRUE ("= 'Y'"),
    IS_FALSE ("= 'N'"),
    IS_NULL ("IS NULL"),
    IS_NOT_NULL ("IS NOT NULL");

    private final String value;

    ComparisonWithoutArgumentOperation(final String value) {
	this.value = value;
    }

    public String getValue() {
	return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
