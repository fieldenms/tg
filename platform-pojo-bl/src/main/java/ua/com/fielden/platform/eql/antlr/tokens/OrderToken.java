package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.eql.antlr.tokens.util.TokensFormatter;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.ORDER;

public final class OrderToken extends AbstractParameterisedEqlToken {

    public final OrderingModel model;

    public OrderToken(final OrderingModel model) {
        super(ORDER, "order");
        this.model = model;
    }

    @Override
    public String parametersText() {
        return TokensFormatter.getInstance().format(model.getTokenSource());
    }

}
