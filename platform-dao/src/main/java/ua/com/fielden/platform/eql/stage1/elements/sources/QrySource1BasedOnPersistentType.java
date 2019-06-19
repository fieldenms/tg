package ua.com.fielden.platform.eql.stage1.elements.sources;

import java.util.Objects;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.eql.meta.PropsResolutionContext;
import ua.com.fielden.platform.eql.meta.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.sources.QrySource2BasedOnPersistentType;

public class QrySource1BasedOnPersistentType extends AbstractQrySource1<QrySource2BasedOnPersistentType> {
    private final Class<? extends AbstractEntity<?>> sourceType;

    public QrySource1BasedOnPersistentType(final Class<? extends AbstractEntity<?>> sourceType, final String alias, final int contextId) {
        super(alias, contextId);
        if (sourceType == null) {
            throw new EqlStage1ProcessingException("Source type is required.");
        }

        this.sourceType = sourceType;
    }

    public QrySource1BasedOnPersistentType(final Class<? extends AbstractEntity<?>> sourceType, final int contextId) {
        this(sourceType, null, contextId);
    }
    
    @Override
    public Class<? extends AbstractEntity<?>> sourceType() {
        return sourceType;
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

    @Override
    public TransformationResult<QrySource2BasedOnPersistentType> transform(final PropsResolutionContext resolutionContext) {
        final QrySource2BasedOnPersistentType transformedSource = new QrySource2BasedOnPersistentType(sourceType(), resolutionContext.getDomainInfo().get(sourceType()), getAlias(), contextId);
        return new TransformationResult<QrySource2BasedOnPersistentType>(transformedSource, resolutionContext.cloneWithAdded(transformedSource, resolutionContext.getResolvedProps()));
    }
}