package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.eql.antlr.tokens.util.TokensFormatter;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.NOTEXISTS;

public final class NotExistsToken extends AbstractParameterisedEqlToken {

    public final QueryModel model;

    public NotExistsToken(final QueryModel model) {
        super(NOTEXISTS, "notExists");
        this.model = model;
    }

    @Override
    public String parametersText() {
        return TokensFormatter.getInstance().format(model.getTokenSource());
    }

}
