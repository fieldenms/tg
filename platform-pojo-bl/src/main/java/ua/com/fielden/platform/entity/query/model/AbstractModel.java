package ua.com.fielden.platform.entity.query.model;

import org.antlr.v4.runtime.Token;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;

public abstract class AbstractModel {
    private final List<Token> tokens = new ArrayList<>();

    protected AbstractModel() {
    }

    public AbstractModel(final List<? extends Token> tokens) {
        this.tokens.addAll(tokens);
    }

    public List<? extends Token> getTokens() {
        return tokens;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((tokens == null) ? 0 : tokens.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof AbstractModel other && Objects.equals(tokens, other.tokens);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (final var token : tokens) {
            sb.append(format("\n\t%s", StringUtils.rightPad(token.toString(), 32, '.')));
        }
        return sb.toString();
    }
}
