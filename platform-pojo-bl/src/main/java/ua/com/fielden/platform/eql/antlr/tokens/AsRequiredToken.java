package ua.com.fielden.platform.eql.antlr.tokens;

import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.wrap;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.ASREQUIRED;

public final class AsRequiredToken extends AbstractParameterisedEqlToken {

    public final String alias;

    public AsRequiredToken(final String alias) {
        super(ASREQUIRED, "asRequired");
        this.alias = requireNonNull(alias);
    }

    @Override
    public String parametersText() {
        return wrap(alias, '"');
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof AsRequiredToken that &&
                Objects.equals(alias, that.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(alias);
    }

}
