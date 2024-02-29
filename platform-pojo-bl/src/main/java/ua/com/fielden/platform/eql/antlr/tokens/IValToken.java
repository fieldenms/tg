package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

public final class IValToken extends CommonToken {

    public final Object value;

    public IValToken(final Object value) {
        super(EQLLexer.IVAL, "iVal");
        this.value = value;
    }

}
