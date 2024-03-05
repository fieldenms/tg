package ua.com.fielden.platform.eql.antlr.tokens;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.ENDASSTR;

public final class EndAsStrToken extends AbstractParameterisedEqlToken {

    public final int length;

    public EndAsStrToken(final int length) {
        super(ENDASSTR, "endAsStr");
        this.length = length;
    }

    @Override
    public String parametersText() {
        return String.valueOf(length);
    }

}
