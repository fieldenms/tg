package ua.com.fielden.platform.eql.antlr.tokens;

import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.wrap;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.IPARAM;

public final class IParamToken extends AbstractParameterisedEqlToken {

    public final String paramName;

    public IParamToken(final String paramName) {
        super(IPARAM, "iParam");
        this.paramName = requireNonNull(paramName);
    }

    @Override
    public String parametersText() {
        return wrap(paramName, '"');
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof IParamToken that &&
                Objects.equals(paramName, that.paramName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(paramName);
    }

}
