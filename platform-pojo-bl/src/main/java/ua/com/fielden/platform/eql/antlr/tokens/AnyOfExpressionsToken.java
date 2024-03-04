package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

import java.util.List;

import static java.util.stream.Collectors.joining;

public final class AnyOfExpressionsToken extends CommonToken {

    public final List<ExpressionModel> models;

    public AnyOfExpressionsToken(final List<ExpressionModel> models) {
        super(EQLLexer.ANYOFEXPRESSIONS, "anyOfExpressions");
        this.models = models;
    }

    @Override
    public String getText() {
        return "allOfExpressions(\n%s\n)".formatted(models.stream()
                .map(m -> m.getTokenSource().tokens().stream().map(Token::getText).collect(joining(" ", "(", ")")))
                .collect(joining("\n")));
    }

}
