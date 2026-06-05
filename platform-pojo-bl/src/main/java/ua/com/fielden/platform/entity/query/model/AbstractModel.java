package ua.com.fielden.platform.entity.query.model;

import com.google.common.collect.ImmutableList;
import org.antlr.v4.runtime.Token;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static ua.com.fielden.platform.entity.query.exceptions.EqlException.requireNotNullArgument;

public abstract class AbstractModel {

    protected final List<? extends Token> tokens;

    public AbstractModel(final List<? extends Token> tokens) {
        requireNotNullArgument(tokens, "tokens");
        this.tokens = ImmutableList.copyOf(tokens);
    }

    public final List<? extends Token> tokens() {
        return tokens;
    }

    @Override
    public int hashCode() {
        return 31 * tokens.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof AbstractModel other && tokens.equals(other.tokens);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (final var token : tokens) {
            sb.append("%n    %s".formatted(StringUtils.rightPad(token.getText(), 32, '.')));
        }
        return sb.toString();
    }

}
