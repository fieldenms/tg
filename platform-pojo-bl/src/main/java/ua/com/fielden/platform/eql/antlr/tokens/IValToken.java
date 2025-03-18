package ua.com.fielden.platform.eql.antlr.tokens;

import java.util.Objects;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.IVAL;
import static ua.com.fielden.platform.eql.antlr.tokens.util.TokensFormatter.getInstance;

public final class IValToken extends AbstractParameterisedEqlToken {
    public static final IValToken NULL = new IValToken(null);

    public final Object value;

    public IValToken(final Object value) {
        super(IVAL, "iVal");
        this.value = value;
    }

    public static IValToken iValToken(final Object value) {
        return value == null ? NULL : new IValToken(value);
    }

    @Override
    public String parametersText() {
        return getInstance().formatLiteral(value);
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof IValToken that &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

}
