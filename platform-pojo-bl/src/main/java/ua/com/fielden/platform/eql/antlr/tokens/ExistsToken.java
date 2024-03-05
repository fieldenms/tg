package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.eql.antlr.tokens.util.TokensFormatter;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.EXISTS;

public final class ExistsToken extends AbstractParameterisedEqlToken {

    public final QueryModel model;

    public ExistsToken(final QueryModel model) {
        super(EXISTS, "exists");
        this.model = model;
    }

    public String parametersText() {
        return " %s ".formatted(TokensFormatter.getInstance().format(model.getTokenSource()));
    }

}
