package ua.com.fielden.platform.equery.tokens.conditions;

public enum ComparisonOperation {
    LIKE("LIKE"), IN("IN"), BETWEEN("BETWEEN"), NE("<>"),
    EQ ("="), GT (">"), GE (">="),
    LT ("<"), LE ("<=");

    private final String value;

    ComparisonOperation(final String value) {
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
