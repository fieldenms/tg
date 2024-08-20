package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.entity.query.model.OrderingModel;

import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.ORDER;
import static ua.com.fielden.platform.eql.antlr.tokens.util.TokensFormatter.getInstance;

public final class OrderToken extends AbstractParameterisedEqlToken {

    public final OrderingModel model;

    public OrderToken(final OrderingModel model) {
        super(ORDER, "order");
        this.model = requireNonNull(model);
    }

    @Override
    public String parametersText() {
        return getInstance().format(model.getTokenSource());
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof OrderToken that &&
                Objects.equals(model, that.model);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(model);
    }

}
