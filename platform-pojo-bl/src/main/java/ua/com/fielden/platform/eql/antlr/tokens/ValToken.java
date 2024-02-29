package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

public final class ValToken extends CommonToken {

    public final Object value;

    public ValToken(final Object value) {
        super(EQLLexer.VAL, "val");
        this.value = value;
    }

    @Override
    public String getText() {
        final String str = value instanceof String s ? "\"%s\"".formatted(s) : String.valueOf(value);
        return "val(%s)".formatted(str);
    }

}
