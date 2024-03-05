package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.eql.antlr.tokens.util.TokensFormatter;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.EXPR;

public final class ExprToken extends AbstractParameterisedEqlToken {

    public final ExpressionModel model;

    public ExprToken(final ExpressionModel model) {
        super(EXPR, "expr");
        this.model = model;
    }


    @Override
    public String parametersText() {
        return " %s ".formatted(TokensFormatter.getInstance().format(model.getTokenSource()));
    }

}
