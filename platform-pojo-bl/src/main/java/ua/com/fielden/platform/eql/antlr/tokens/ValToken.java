package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.eql.antlr.tokens.util.TokensFormatter;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.VAL;

public final class ValToken extends AbstractParameterisedEqlToken {
    public static final ValToken NULL = new ValToken(null);

    public final Object value;

    public ValToken(final Object value) {
        super(VAL, "val");
        this.value = value;
    }

    public static ValToken valToken(final Object value) {
        return value == null ? NULL : new ValToken(value);
    }

    public String parametersText() {
        return TokensFormatter.getInstance().formatLiteral(value);
    }

}
