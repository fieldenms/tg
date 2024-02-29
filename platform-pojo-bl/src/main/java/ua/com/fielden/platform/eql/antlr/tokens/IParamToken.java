package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

public final class IParamToken extends CommonToken {

    public final String paramName;

    public IParamToken(final String paramName) {
        super(EQLLexer.IPARAM, "iParam");
        this.paramName = paramName;
    }

}
