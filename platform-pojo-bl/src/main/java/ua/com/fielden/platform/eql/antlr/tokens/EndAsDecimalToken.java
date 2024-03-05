package ua.com.fielden.platform.eql.antlr.tokens;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.ENDASDECIMAL;

public final class EndAsDecimalToken extends AbstractParameterisedEqlToken {

    public final Integer precision;
    public final Integer scale;

    public EndAsDecimalToken(final Integer precision, final Integer scale) {
        super(ENDASDECIMAL, "endAsDecimal");
        this.precision = precision;
        this.scale = scale;
    }

    @Override
    public String parametersText() {
        return "precision=%s, scale=%s".formatted(precision, scale);
    }

}
