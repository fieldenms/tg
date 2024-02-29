package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

public final class EndAsDecimalToken extends CommonToken {

    public final Integer precision;
    public final Integer scale;

    public EndAsDecimalToken(final Integer precision, final Integer scale) {
        super(EQLLexer.ENDASDECIMAL, "endAsDecimal");
        this.precision = precision;
        this.scale = scale;
    }

}
