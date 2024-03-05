package ua.com.fielden.platform.eql.antlr.tokens;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.PARAM;

public final class ParamToken extends AbstractParameterisedEqlToken {

    public final String paramName;

    public ParamToken(final String paramName) {
        super(PARAM, "param");
        this.paramName = paramName;
    }

    @Override
    public String parametersText() {
        return paramName;
    }

}
