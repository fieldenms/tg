package ua.com.fielden.platform.eql.antlr.tokens;

import java.util.Objects;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.ENDASDECIMAL;

public final class EndAsDecimalToken extends AbstractParameterisedEqlToken {

    public final int precision;
    public final int scale;

    public EndAsDecimalToken(final int precision, final int scale) {
        super(ENDASDECIMAL, "endAsDecimal");
        this.precision = precision;
        this.scale = scale;
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
