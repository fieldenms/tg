package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.eql.antlr.tokens.util.TokensFormatter;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.IVAL;

public final class IValToken extends AbstractParameterisedEqlToken {
    public static final IValToken NULL = new IValToken(null);

    public final Object value;

    public IValToken(final Object value) {
        super(IVAL, "iVal");
        this.value = value;
    }

    public static IValToken iValToken(final Object value) {
        return value == null ? NULL : new IValToken(value);
    }

    @Override
    public String parametersText() {
        return TokensFormatter.getInstance().formatLiteral(value);
    }

}
