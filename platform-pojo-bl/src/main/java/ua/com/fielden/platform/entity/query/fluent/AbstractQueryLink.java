package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.exceptions.EqlException;

import java.util.Objects;

abstract class AbstractQueryLink {

    public final EqlSentenceBuilder builder;

    protected AbstractQueryLink(final EqlSentenceBuilder builder) {
        if (builder == null) {
            throw new EqlException("Invalid argument -- tokens should not be null.");
        }

        this.builder = builder;
    }

    public EqlSentenceBuilder getBuilder() {
        return builder;
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
