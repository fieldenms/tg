package ua.com.fielden.platform.entity.query.fluent;

public sealed interface Limit {

    static Limit all() {
        return All.INSTANCE;
    }

    static Limit count(final long n) {
        return new Count(n);
    }

    final class All implements Limit {
        private static final All INSTANCE = new All();
        private All() {}
    }

    record Count(long n) implements Limit {}

}
