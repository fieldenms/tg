package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;
import ua.com.fielden.platform.eql.antlr.tokens.util.TokensFormatter;

import java.util.List;

import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.ANYOFMODELS;

public final class AnyOfModelsToken extends AbstractParameterisedEqlToken {

    public final List<PrimitiveResultQueryModel> models;

    public AnyOfModelsToken(final List<PrimitiveResultQueryModel> models) {
        super(ANYOFMODELS, "anyOfModels");
        this.models = models;
    }

    @Override
    public String parametersText() {
        return models.stream()
                .map(m -> "(%s)".formatted(TokensFormatter.getInstance().format(m.getTokenSource())))
                .collect(joining(",\n", "\n", "\n"));
    }

}
