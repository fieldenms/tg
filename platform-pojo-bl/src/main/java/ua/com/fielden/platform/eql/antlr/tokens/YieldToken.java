package ua.com.fielden.platform.eql.antlr.tokens;

import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.YIELD;

public final class YieldToken extends AbstractParameterisedEqlToken {

    public final String yieldName;

    public YieldToken(final String yieldName) {
        super(YIELD, "yield");
        this.yieldName = requireNonNull(yieldName);
    }

    public String parametersText() {
        return "\"%s\"".formatted(yieldName);
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof YieldToken that &&
                Objects.equals(yieldName, that.yieldName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(yieldName);
    }

}
