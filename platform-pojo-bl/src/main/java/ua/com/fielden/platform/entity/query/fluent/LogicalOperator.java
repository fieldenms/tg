package ua.com.fielden.platform.entity.query.fluent;

public enum LogicalOperator {
    AND("AND"), OR("OR");
    private final String value;

    LogicalOperator(final String value) {
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