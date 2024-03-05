package ua.com.fielden.platform.eql.antlr.tokens;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.TO;

public final class ToToken extends AbstractParameterisedEqlToken {

    public final int value;

    public ToToken(final int value) {
        super(TO, "to(%s)".formatted(value));
        this.value = value;
    }

    @Override
    public String parametersText() {
        return String.valueOf(value);
    }

}
