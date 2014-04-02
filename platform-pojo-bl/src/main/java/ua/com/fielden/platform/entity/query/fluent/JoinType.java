package ua.com.fielden.platform.entity.query.fluent;

public enum JoinType {
    LJ("LEFT JOIN"), IJ("INNER JOIN");

    private final String value;

    JoinType(final String value) {
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