package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.entity.query.model.SingleResultQueryModel;

import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.ANY;
import static ua.com.fielden.platform.eql.antlr.tokens.util.TokensFormatter.getInstance;

public final class AnyToken extends AbstractParameterisedEqlToken {

    public final SingleResultQueryModel model;

    public AnyToken(final SingleResultQueryModel model) {
        super(ANY, "any");
        this.model = requireNonNull(model);
    }

    @Override
    public String parametersText() {
        return getInstance().format(model.getTokenSource());
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof AnyToken that &&
                Objects.equals(model, that.model);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(model);
    }

}
