package ua.com.fielden.platform.eql.antlr.tokens;

import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.ENDASDECIMAL;

public final class EndAsDecimalToken extends AbstractParameterisedEqlToken {

    public final Integer precision;
    public final Integer scale;

    public EndAsDecimalToken(final Integer precision, final Integer scale) {
        super(ENDASDECIMAL, "endAsDecimal");
        this.precision = requireNonNull(precision);
        this.scale = requireNonNull(scale);
    }

    @Override
    public String parametersText() {
        return "precision=%s, scale=%s".formatted(precision, scale);
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof EndAsDecimalToken that &&
                Objects.equals(precision, that.precision) &&
                Objects.equals(scale, that.scale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(precision, scale);
    }

}
