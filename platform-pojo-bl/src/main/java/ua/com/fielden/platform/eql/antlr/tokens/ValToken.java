package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.eql.antlr.tokens.util.TokensFormatter;

import static java.lang.String.valueOf;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.VAL;

public final class ValToken extends AbstractParameterisedEqlToken {

    public final Object value;

    public ValToken(final Object value) {
        super(VAL, "val");
        this.value = value;
    }

    public String parametersText() {
        return TokensFormatter.getInstance().formatLiteral(value);
    }

}
