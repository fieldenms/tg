package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.entity.query.fluent.Limit;

import static ua.com.fielden.platform.entity.query.exceptions.EqlException.requireNotNullArgument;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.LIMIT;

public abstract sealed class LimitToken extends AbstractParameterisedEqlToken {

    LimitToken() {
        super(LIMIT, "limit");
    }

    public static WithLong limit(final long limit) {
        return new WithLong(limit);
    }

    public static WithLimit limit(final Limit limit) {
        return new WithLimit(limit);
    }

    public static final class WithLong extends LimitToken {
        public final long limit;

        public WithLong(final long limit) {
            this.limit = limit;
        }

        @Override
        public String parametersText() {
            return String.valueOf(limit);
        }

        @Override
        public boolean equals(final Object obj) {
            return obj == this || obj instanceof WithLong that && limit == that.limit;
        }

        @Override
        public int hashCode() {
            return Long.hashCode(limit);
        }
    }

    public static final class WithLimit extends LimitToken {
        public final Limit limit;

        public WithLimit(final Limit limit) {
            this.limit = limit;
        }

        @Override
        public String parametersText() {
            return switch (limit) {
                case Limit.All $ -> "ALL";
                case Limit.Count (long n) -> String.valueOf(n);
            };
        }

        @Override
        public boolean equals(final Object obj) {
            return obj == this || obj instanceof WithLimit that && limit.equals(that.limit);
        }

        @Override
        public int hashCode() {
            return limit.hashCode();
        }
    }

}
