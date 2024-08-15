package ua.com.fielden.platform.entity.query.fluent.enums;

public enum JoinType {
    LJ("LEFT JOIN"), IJ("INNER JOIN");

    public final String value;

    JoinType(final String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

}
