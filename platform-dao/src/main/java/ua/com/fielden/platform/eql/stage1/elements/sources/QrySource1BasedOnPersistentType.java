package ua.com.fielden.platform.eql.stage1.elements.sources;

import java.util.Objects;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage2.elements.sources.QrySource2BasedOnPersistentType;

public class QrySource1BasedOnPersistentType extends AbstractQrySource1<QrySource2BasedOnPersistentType> {
    private final Class<? extends AbstractEntity<?>> sourceType;

    public QrySource1BasedOnPersistentType(final Class<? extends AbstractEntity<?>> sourceType, final String alias, final int contextId) {
        super(alias, contextId);
        this.sourceType = Objects.requireNonNull(sourceType);
    }

    @Override
    public Class<? extends AbstractEntity<?>> sourceType() {
        return sourceType;
    }

    @Override
    public QrySource2BasedOnPersistentType transform(final PropsResolutionContext context) {
        return new QrySource2BasedOnPersistentType(sourceType(), context.getDomainInfo().getEntityInfo(sourceType()), alias, getTransformedContextId(context));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + sourceType.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!super.equals(obj)) {
            return false;
        }

        if (!(obj instanceof QrySource1BasedOnPersistentType)) {
            return false;
        }

        final QrySource1BasedOnPersistentType other = (QrySource1BasedOnPersistentType) obj;

        return Objects.equals(sourceType, other.sourceType);
    }
}