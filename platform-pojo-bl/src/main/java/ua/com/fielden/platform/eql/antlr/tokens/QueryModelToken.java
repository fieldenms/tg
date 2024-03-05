package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.Token;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.eql.antlr.tokens.util.TokensFormatter;

import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.MODEL;

public final class QueryModelToken<T extends QueryModel<?>> extends AbstractParameterisedEqlToken {

    public final T model;

    public QueryModelToken(final T model) {
        super(MODEL, "model");
        this.model = model;
    }

    @Override
    public String parametersText() {
        return TokensFormatter.getInstance().format(model.getTokenSource());
    }

}
