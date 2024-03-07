package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.eql.antlr.tokens.util.TokensFormatter;

import java.util.List;

import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.NOTEXISTSANYOF;

public final class NotExistsAnyOfToken extends AbstractParameterisedEqlToken {

    public final List<? extends QueryModel> models;

    public NotExistsAnyOfToken(final List<? extends QueryModel> models) {
        super(NOTEXISTSANYOF, "notExistsAnyOf");
        this.models = models;
    }

    @Override
    public String parametersText() {
        return models.stream()
                .map(m -> "(%s)".formatted(TokensFormatter.getInstance().format(m.getTokenSource())))
                .collect(joining(",\n", "\n", "\n"));
    }

}
