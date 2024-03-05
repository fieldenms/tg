package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.entity.query.model.SingleResultQueryModel;
import ua.com.fielden.platform.eql.antlr.tokens.util.TokensFormatter;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.ALL;

public final class AllToken extends AbstractParameterisedEqlToken {

    public final SingleResultQueryModel model;

    public AllToken(final SingleResultQueryModel model) {
        super(ALL, "all");
        this.model = model;
    }

    @Override
    public String parametersText() {
        return TokensFormatter.getInstance().format(model.getTokenSource());
    }

}
