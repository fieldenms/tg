package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

public final class EndAsStrToken extends CommonToken {

    public final int length;

    public EndAsStrToken(final int length) {
        super(EQLLexer.ENDASSTR, "endAsStr");
        this.length = length;
    }

}
