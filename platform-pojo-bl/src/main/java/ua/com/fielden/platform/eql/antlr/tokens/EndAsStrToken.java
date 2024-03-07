package ua.com.fielden.platform.eql.antlr.tokens;

import java.util.Objects;

import static java.lang.String.valueOf;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.ENDASSTR;

public final class EndAsStrToken extends AbstractParameterisedEqlToken {

    public final int length;

    public EndAsStrToken(final int length) {
        super(ENDASSTR, "endAsStr");
        this.length = length;
    }

    @Override
    public String parametersText() {
        return valueOf(length);
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof EndAsStrToken that &&
                length == that.length;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(length);
    }

}
