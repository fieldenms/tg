package ua.com.fielden.platform.entity.query.fluent;

public enum ComparisonOperator {
    NE("<>"), EQ("="), GT(">"), GE(">="), LT("<"), LE("<=");
    private final String value;

    ComparisonOperator(final String value) {
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
