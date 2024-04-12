package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.entity.query.model.ExpressionModel;

import java.util.Objects;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.EXPR;
import static ua.com.fielden.platform.eql.antlr.tokens.util.TokensFormatter.getInstance;

public final class ExprToken extends AbstractParameterisedEqlToken {

    public final ExpressionModel model;

    public ExprToken(final ExpressionModel model) {
        super(EXPR, "expr");
        this.model = model;
    }

    @Override
    public String parametersText() {
        return " %s ".formatted(getInstance().format(model.getTokenSource()));
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof ExprToken that &&
                Objects.equals(model, that.model);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(model);
    }

}
