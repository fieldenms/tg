package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.entity.query.model.ExpressionModel;

import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.ALLOFEXPRESSIONS;
import static ua.com.fielden.platform.eql.antlr.tokens.util.TokensFormatter.getInstance;

public final class AllOfExpressionsToken extends AbstractParameterisedEqlToken {

    public final List<ExpressionModel> models;

    public AllOfExpressionsToken(final List<ExpressionModel> models) {
        super(ALLOFEXPRESSIONS, "allOfExpressions");
        this.models = models;
    }

    @Override
    public String parametersText() {
        return models.stream()
                .map(m -> "(%s)".formatted(getInstance().format(m.getTokenSource())))
                .collect(joining(",\n", "\n", "\n"));
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof AllOfExpressionsToken that &&
                Objects.equals(models, that.models);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(models);
    }

}
