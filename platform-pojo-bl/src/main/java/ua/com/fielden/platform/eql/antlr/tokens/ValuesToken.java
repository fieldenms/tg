package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.eql.antlr.tokens.util.TokensFormatter;
import ua.com.fielden.platform.utils.CollectionUtil;

import java.util.List;

import static java.util.List.copyOf;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.VALUES;

public final class ValuesToken extends AbstractParameterisedEqlToken {

    public final List<Object> values;

    public ValuesToken(final List<Object> values) {
        super(VALUES, "values");
        this.values = copyOf(values);
    }

    public String parametersText() {
        final var fmt = TokensFormatter.getInstance();
        return CollectionUtil.toString(values, fmt::formatLiteral, ", ");
    }

}
