package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.entity.AbstractEntity;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.MODELASENTITY;

public final class ModelAsEntityToken extends AbstractParameterisedEqlToken {

    public final Class<? extends AbstractEntity<?>> entityType;

    public ModelAsEntityToken(final Class<? extends AbstractEntity<?>> entityType) {
        super(MODELASENTITY, "modelAsEntity");
        this.entityType = entityType;
    }

    public String parametersText() {
        return entityType.getSimpleName();
    }

}
