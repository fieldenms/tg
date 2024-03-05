package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.entity.query.model.SingleResultQueryModel;
import ua.com.fielden.platform.eql.antlr.tokens.util.TokensFormatter;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.ANY;

public final class AnyToken extends AbstractParameterisedEqlToken {

    public final SingleResultQueryModel model;

    public AnyToken(final SingleResultQueryModel model) {
        super(ANY, "any");
        this.model = model;
    }

    @Override
    public String parametersText() {
        return TokensFormatter.getInstance().format(model.getTokenSource());
    }

}
