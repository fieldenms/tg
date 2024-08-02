package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.entity.AbstractEntity;

import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.MODELASENTITY;

public final class ModelAsEntityToken extends AbstractParameterisedEqlToken {

    public final Class<? extends AbstractEntity<?>> entityType;

    public ModelAsEntityToken(final Class<? extends AbstractEntity<?>> entityType) {
        super(MODELASENTITY, "modelAsEntity");
        this.entityType = requireNonNull(entityType);
    }

    public String parametersText() {
        return entityType.getSimpleName();
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof ModelAsEntityToken that &&
                Objects.equals(entityType, that.entityType);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(entityType);
    }

}
