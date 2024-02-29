package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

public final class ModelAsEntityToken extends CommonToken {

    public final Class<? extends AbstractEntity<?>> entityType;

    public ModelAsEntityToken(final Class<? extends AbstractEntity<?>> entityType) {
        super(EQLLexer.MODELASENTITY, "modelAsEntity");
        this.entityType = entityType;
    }

    @Override
    public String getText() {
        return "modelAsEntity(%s)".formatted(entityType.getSimpleName());
    }

}
