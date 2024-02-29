package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

public final class ParamToken extends CommonToken {

    public final String paramName;

    public ParamToken(final String paramName) {
        super(EQLLexer.PARAM, "param");
        this.paramName = paramName;
    }

}
