package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.eql.antlr.tokens.util.TokensFormatter;

import java.util.List;

import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.NOTEXISTSALLOF;

public final class NotExistsAllOfToken extends AbstractParameterisedEqlToken {

    public final List<QueryModel> models;

    public NotExistsAllOfToken(final List<QueryModel> models) {
        super(NOTEXISTSALLOF, "notExistsAllOf");
        this.models = models;
    }

    @Override
    public String parametersText() {
        return models.stream()
                .map(m -> "(%s)".formatted(TokensFormatter.getInstance().format(m.getTokenSource())))
                .collect(joining(",\n", "\n", "\n"));
    }

}
