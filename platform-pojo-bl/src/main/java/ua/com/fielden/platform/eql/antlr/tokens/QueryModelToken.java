package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.entity.query.model.QueryModel;

import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.MODEL;
import static ua.com.fielden.platform.eql.antlr.tokens.util.TokensFormatter.getInstance;

public final class QueryModelToken<T extends QueryModel<?>> extends AbstractParameterisedEqlToken {

    public final T model;

    public QueryModelToken(final T model) {
        super(MODEL, "model");
        this.model = requireNonNull(model);
    }

    @Override
    public String parametersText() {
        return getInstance().format(model.getTokenSource());
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof QueryModelToken that &&
                Objects.equals(model, that.model);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(model);
    }

}
