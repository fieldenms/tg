package ua.com.fielden.platform.entity.query.fluent;

public enum QueryTokens {
    ASC("ASC"), DESC("DESC"), ON("ON"), WHERE("WHERE"), YIELD("SELECT"), GROUP_BY("GROUP BY"), ORDER_BY("ORDER BY"), FROM("FROM");

    private final String value;

    QueryTokens(final String value) {
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