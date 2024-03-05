package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.eql.antlr.tokens.util.TokensFormatter;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.IVAL;

public final class IValToken extends AbstractParameterisedEqlToken {

    public final Object value;

    public IValToken(final Object value) {
        super(IVAL, "iVal");
        this.value = value;
    }

    @Override
    public String parametersText() {
        return TokensFormatter.getInstance().formatLiteral(value);
    }

}
