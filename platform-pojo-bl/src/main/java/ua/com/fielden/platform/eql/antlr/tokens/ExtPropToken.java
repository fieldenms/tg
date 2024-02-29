package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

public final class ExtPropToken extends CommonToken {

    public final String propPath;

    public ExtPropToken(final String propPath) {
        super(EQLLexer.EXTPROP, "extProp");
        this.propPath = propPath;
    }

}
