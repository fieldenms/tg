package ua.com.fielden.platform.eql.antlr.tokens;

import java.util.Objects;

import static java.lang.String.valueOf;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.TO;

public final class ToToken extends AbstractParameterisedEqlToken {

    public final int value;

    public ToToken(final int value) {
        super(TO, "to(%s)".formatted(value));
        this.value = value;
    }

    @Override
    public String parametersText() {
        return valueOf(value);
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof ToToken that &&
                value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

}
