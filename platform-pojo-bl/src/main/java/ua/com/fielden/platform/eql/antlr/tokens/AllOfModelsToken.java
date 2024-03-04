package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

import java.util.List;

import static java.util.stream.Collectors.joining;

public final class AllOfModelsToken extends CommonToken {

    public final List<PrimitiveResultQueryModel> models;

    public AllOfModelsToken(final List<PrimitiveResultQueryModel> models) {
        super(EQLLexer.ALLOFMODELS, "allOfModels");
        this.models = models;
    }

    @Override
    public String getText() {
        return "allOfModels(\n%s\n)".formatted(models.stream()
                .map(m -> m.getTokenSource().tokens().stream().map(Token::getText).collect(joining(" ", "(", ")")))
                .collect(joining("\n")));
    }

}
