package ua.com.fielden.platform.eql.antlr.tokens;

import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.PARAM;

public final class ParamToken extends AbstractParameterisedEqlToken {

    public final String paramName;

    public ParamToken(final String paramName) {
        super(PARAM, "param");
        this.paramName = requireNonNull(paramName);
    }

    @Override
    public String parametersText() {
        return paramName;
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof ParamToken that &&
                Objects.equals(paramName, that.paramName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(paramName);
    }

}
