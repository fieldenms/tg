package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.utils.CollectionUtil;

import java.util.List;
import java.util.Objects;

import static java.util.List.copyOf;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.VALUES;
import static ua.com.fielden.platform.eql.antlr.tokens.util.TokensFormatter.getInstance;

public final class ValuesToken extends AbstractParameterisedEqlToken {

    public final List<Object> values;

    public ValuesToken(final List<Object> values) {
        super(VALUES, "values");
        this.values = copyOf(values);
    }

    public String parametersText() {
        final var fmt = getInstance();
        return CollectionUtil.toString(values, fmt::formatLiteral, ", ");
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof ValuesToken that &&
                Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(values);
    }

}
