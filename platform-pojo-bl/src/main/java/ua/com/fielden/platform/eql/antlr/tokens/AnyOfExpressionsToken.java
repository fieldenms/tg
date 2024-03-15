package ua.com.fielden.platform.eql.antlr.tokens;

import com.google.common.collect.ImmutableList;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;

import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.ANYOFEXPRESSIONS;
import static ua.com.fielden.platform.eql.antlr.tokens.util.TokensFormatter.getInstance;

public final class AnyOfExpressionsToken extends AbstractParameterisedEqlToken {

    public final List<ExpressionModel> models;

    public AnyOfExpressionsToken(final List<ExpressionModel> models) {
        super(ANYOFEXPRESSIONS, "anyOfExpressions");
        this.models = ImmutableList.copyOf(models);
    }

    @Override
    public String parametersText() {
        return models.stream()
                .map(m -> "(%s)".formatted(getInstance().format(m.getTokenSource())))
                .collect(joining(",\n", "\n", "\n"));
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof AnyOfExpressionsToken that &&
                Objects.equals(models, that.models);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(models);
    }

}
