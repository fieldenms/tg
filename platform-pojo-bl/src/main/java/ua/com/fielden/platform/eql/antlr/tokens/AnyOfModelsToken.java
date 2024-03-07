package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;

import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.ANYOFMODELS;
import static ua.com.fielden.platform.eql.antlr.tokens.util.TokensFormatter.getInstance;

public final class AnyOfModelsToken extends AbstractParameterisedEqlToken {

    public final List<PrimitiveResultQueryModel> models;

    public AnyOfModelsToken(final List<PrimitiveResultQueryModel> models) {
        super(ANYOFMODELS, "anyOfModels");
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
        return this == o || o instanceof AnyOfModelsToken that &&
                Objects.equals(models, that.models);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(models);
    }

}
