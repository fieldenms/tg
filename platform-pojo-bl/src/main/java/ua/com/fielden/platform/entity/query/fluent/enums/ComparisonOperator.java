package ua.com.fielden.platform.entity.query.fluent.enums;

public enum ComparisonOperator {
    NE("<>"), EQ("="), GT(">"), GE(">="), LT("<"), LE("<=");

    public final String value;

    ComparisonOperator(final String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

}
