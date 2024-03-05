package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.eql.antlr.tokens.util.TokensFormatter;
import ua.com.fielden.platform.utils.CollectionUtil;

import java.util.List;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.ANYOFVALUES;

public final class AnyOfValuesToken extends AbstractParameterisedEqlToken {

    public final List<Object> values;

    public AnyOfValuesToken(final List<Object> values) {
        super(ANYOFVALUES, "anyOfValues");
        this.values = values;
    }

    public String parametersText() {
        final var fmt = TokensFormatter.getInstance();
        return CollectionUtil.toString(values, fmt::formatLiteral, ", ");
    }

}
