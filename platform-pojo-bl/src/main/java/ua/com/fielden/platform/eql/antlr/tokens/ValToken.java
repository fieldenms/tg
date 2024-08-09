package ua.com.fielden.platform.eql.antlr.tokens;

import java.util.Objects;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.VAL;
import static ua.com.fielden.platform.eql.antlr.tokens.util.TokensFormatter.getInstance;

public final class ValToken extends AbstractParameterisedEqlToken {
    public static final ValToken NULL = new ValToken(null);

    public final Object value;

    public ValToken(final Object value) {
        super(VAL, "val");
        this.value = value;
    }

    public static ValToken valToken(final Object value) {
        return value == null ? NULL : new ValToken(value);
    }

    public String parametersText() {
        return getInstance().formatLiteral(value);
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof ValToken that &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

}
