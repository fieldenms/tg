package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.entity.query.model.SingleResultQueryModel;

import java.util.Objects;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.ALL;
import static ua.com.fielden.platform.eql.antlr.tokens.util.TokensFormatter.getInstance;

public final class AllToken extends AbstractParameterisedEqlToken {

    public final SingleResultQueryModel model;

    public AllToken(final SingleResultQueryModel model) {
        super(ALL, "all");
        this.model = model;
    }

    @Override
    public String parametersText() {
        return getInstance().format(model.getTokenSource());
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof AllToken that &&
                Objects.equals(model, that.model);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(model);
    }

}
