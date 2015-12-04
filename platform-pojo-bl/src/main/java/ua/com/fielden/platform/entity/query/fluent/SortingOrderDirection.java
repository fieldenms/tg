package ua.com.fielden.platform.entity.query.fluent;

public enum SortingOrderDirection {
    ASC("ASC"), DESC("DESC");

    private final String value;

    SortingOrderDirection(final String value) {
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