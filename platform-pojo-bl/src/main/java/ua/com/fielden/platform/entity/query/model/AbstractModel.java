package ua.com.fielden.platform.entity.query.model;

import org.apache.commons.lang3.StringUtils;
import ua.com.fielden.platform.eql.antlr.tokens.util.ListTokenSource;

import static ua.com.fielden.platform.entity.query.exceptions.EqlException.requireNotNullArgument;

public abstract class AbstractModel {
    protected final ListTokenSource tokenSource;

    public AbstractModel(final ListTokenSource tokenSource) {
        requireNotNullArgument(tokenSource, "tokenSource");
        this.tokenSource = tokenSource;
    }

    public final ListTokenSource getTokenSource() {
        return tokenSource.restart();
    }

    @Override
    public int hashCode() {
        return 31 * tokenSource.tokens().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof AbstractModel other && tokenSource.equalTokens(other.tokenSource);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (final var token : tokenSource.tokens()) {
            sb.append("\n    %s".formatted(StringUtils.rightPad(token.getText(), 32, '.')));
        }
        return sb.toString();
    }

}
