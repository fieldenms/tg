package ua.com.fielden.platform.entity.query.fluent.enums;

public enum LogicalOperator {
    AND("AND"), OR("OR");

    public final String value;

    LogicalOperator(final String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

}
