package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.eql.antlr.tokens.util.TokensFormatter;

import java.util.List;

import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.ANYOFEXPRESSIONS;

public final class AnyOfExpressionsToken extends AbstractParameterisedEqlToken {

    public final List<ExpressionModel> models;

    public AnyOfExpressionsToken(final List<ExpressionModel> models) {
        super(ANYOFEXPRESSIONS, "anyOfExpressions");
        this.models = models;
    }

    @Override
    public String parametersText() {
        return models.stream()
                .map(m -> "(%s)".formatted(TokensFormatter.getInstance().format(m.getTokenSource())))
                .collect(joining(",\n", "\n", "\n"));
    }

}
