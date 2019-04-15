package ua.com.fielden.platform.entity.query.fluent;

import static java.lang.String.format;

/**
 * A structure that expresses configuration parameters for operator {@code LIKE}.
 *
 * @author TG Team
 *
 */
public class LikeOptions {
    public final boolean negated;
    public final boolean caseInsensitive;
    public final boolean withCast;
    
    private LikeOptions(final Builder builder) {
        negated = builder.negated;
        caseInsensitive = builder.caseInsensitive;
        withCast = builder.withCast;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (caseInsensitive ? 1231 : 1237);
        result = prime * result + (negated ? 1231 : 1237);
        result = prime * result + (withCast ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof LikeOptions)) {
            return false;
        }

        final LikeOptions other = (LikeOptions) obj;
        return negated == other.negated && caseInsensitive == other.caseInsensitive && withCast == other.withCast;
    }
    
    @Override
    public String toString() {
        return format("negated: %s caseInsensitive: %s withCast: %s", negated, caseInsensitive, withCast);
    }

    public static LikeOptions.Builder options() {
        return new LikeOptions.Builder();
    }

    public static class Builder {
        private boolean negated = false;
        private boolean caseInsensitive = false;
        private boolean withCast = false;
        
        public Builder negated() {
            negated = true;
            return this;
        }

        public Builder caseInsensitive() {
            caseInsensitive = true;
            return this;
        }
        
        public Builder withCast() {
            withCast = true;
            return this;
        }

        public LikeOptions build() {
            return new LikeOptions(this);
        }

    }
}