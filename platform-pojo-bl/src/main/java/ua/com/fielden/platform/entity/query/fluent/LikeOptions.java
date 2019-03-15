package ua.com.fielden.platform.entity.query.fluent;

public class LikeOptions {
    public final boolean negated;
    public final boolean caseInsensitive;
    
    private LikeOptions(final Builder builder) {
        negated = builder.negated;
        caseInsensitive = builder.caseInsensitive;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (caseInsensitive ? 1231 : 1237);
        result = prime * result + (negated ? 1231 : 1237);
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
        
        LikeOptions other = (LikeOptions) obj;
        
        return negated == other.negated && caseInsensitive == other.caseInsensitive;
    }

    public static LikeOptions.Builder options() {
        return new LikeOptions.Builder();
    }

    public static class Builder {
        private boolean negated = false;
        private boolean caseInsensitive = false;

        
        public Builder negated() {
            negated = true;
            return this;
        }

        public Builder caseInsensitive() {
            caseInsensitive = true;
            return this;
        }
        
        public LikeOptions build() {
            return new LikeOptions(this);
        }

    }
}