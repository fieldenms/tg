package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.utils.CollectionUtil;

import java.util.List;
import java.util.Objects;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.ALLOFVALUES;
import static ua.com.fielden.platform.eql.antlr.tokens.util.TokensFormatter.getInstance;

public final class AllOfValuesToken extends AbstractParameterisedEqlToken {

    public final List<Object> values;

    public AllOfValuesToken(final List<Object> values) {
        super(ALLOFVALUES, "allOfValues");
        this.values = values;
    }

    public String parametersText() {
        final var fmt = getInstance();
        return CollectionUtil.toString(values, fmt::formatLiteral, ", ");
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof AllOfValuesToken that &&
                Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(values);
    }

}
