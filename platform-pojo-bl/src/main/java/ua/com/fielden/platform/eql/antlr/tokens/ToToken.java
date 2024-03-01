package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

public final class ToToken extends CommonToken {

    public final int value;

    public ToToken(final int value) {
        super(EQLLexer.TO, "to(%s)".formatted(value));
        this.value = value;
    }

}
