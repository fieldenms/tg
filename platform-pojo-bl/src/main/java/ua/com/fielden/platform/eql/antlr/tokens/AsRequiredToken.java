package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

public final class AsRequiredToken extends CommonToken {

    public final String alias;

    public AsRequiredToken(final String alias) {
        super(EQLLexer.ASREQUIRED, "asRequired");
        this.alias = alias;
    }

}
