package ua.com.fielden.platform.entity.query.fluent;

public enum SortingOrder {
    ASC("ASC"), DESC("DESC");

    private final String value;

    SortingOrder(final String value) {
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