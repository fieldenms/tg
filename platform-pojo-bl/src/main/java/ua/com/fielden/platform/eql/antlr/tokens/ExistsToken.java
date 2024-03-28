package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.entity.query.model.QueryModel;

import java.util.Objects;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.EXISTS;
import static ua.com.fielden.platform.eql.antlr.tokens.util.TokensFormatter.getInstance;

public final class ExistsToken extends AbstractParameterisedEqlToken {

    public final QueryModel model;

    public ExistsToken(final QueryModel model) {
        super(EXISTS, "exists");
        this.model = model;
    }

    public String parametersText() {
        return " %s ".formatted(getInstance().format(model.getTokenSource()));
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof ExistsToken that &&
                Objects.equals(model, that.model);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(model);
    }

}
