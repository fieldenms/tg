package ua.com.fielden.platform.entity.query.model;

import org.apache.commons.lang3.StringUtils;
import ua.com.fielden.platform.eql.antlr.ListTokenSource;

import java.util.Objects;

import static java.lang.String.format;

public abstract class AbstractModel {
    protected final ListTokenSource tokenSource;

    public AbstractModel(final ListTokenSource tokenSource) {
        this.tokenSource = tokenSource;
    }

    public final ListTokenSource getTokenSource() {
        return tokenSource.restart();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((tokenSource == null) ? 0 : tokenSource.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof AbstractModel other && Objects.equals(tokenSource, other.tokenSource);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (final var token : tokenSource.tokens()) {
            sb.append(format("\n\t%s", StringUtils.rightPad(token.getText(), 32, '.')));
        }
        return sb.toString();
    }
}
