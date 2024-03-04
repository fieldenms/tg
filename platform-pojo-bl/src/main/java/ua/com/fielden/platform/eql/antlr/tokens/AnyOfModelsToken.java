package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

import java.util.List;

import static java.util.stream.Collectors.joining;

public final class AnyOfModelsToken extends CommonToken {

    public final List<PrimitiveResultQueryModel> models;

    public AnyOfModelsToken(final List<PrimitiveResultQueryModel> models) {
        super(EQLLexer.ANYOFMODELS, "anyOfModels");
        this.models = models;
    }

    @Override
    public String getText() {
        return "anyOfModels(\n%s\n)".formatted(models.stream()
                .map(m -> m.getTokenSource().tokens().stream().map(Token::getText).collect(joining(" ", "(", ")")))
                .collect(joining("\n")));
    }

}
