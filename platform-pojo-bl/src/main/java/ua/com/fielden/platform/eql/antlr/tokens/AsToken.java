package ua.com.fielden.platform.eql.antlr.tokens;

import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.wrap;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.AS;

public final class AsToken extends AbstractParameterisedEqlToken {

    public final String alias;

    public AsToken(final String alias) {
        super(AS, "as");
        this.alias = alias;
    }

    public String parametersText() {
        return wrap(alias, '"');
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof AsToken that &&
                Objects.equals(alias, that.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(alias);
    }

}
