package ua.com.fielden.platform.entity.query.fluent;

import java.util.Objects;

import static ua.com.fielden.platform.entity.query.exceptions.EqlException.requireNotNullArgument;

abstract class AbstractQueryLink {

    public final EqlSentenceBuilder builder;

    protected AbstractQueryLink(final EqlSentenceBuilder builder) {
        requireNotNullArgument(builder, "builder");
        this.builder = builder;
    }

    @Override
    public String toString() {
        return builder.toString();
    }

    @Override
    public int hashCode() {
        return 31 * builder.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final AbstractQueryLink that = (AbstractQueryLink) obj;
        return Objects.equals(this.builder, that.builder);
    }

}
