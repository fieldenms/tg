package ua.com.fielden.platform.eql.stage1.elements;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.stage2.elements.QueryBasedSource2;

public class QrySource1BasedOnSyntheticType extends AbstractSource1<QueryBasedSource2> {
    private final Class<? extends AbstractEntity<?>> sourceType;

    public QrySource1BasedOnSyntheticType(final Class<? extends AbstractEntity<?>> sourceType, final String alias) {
        super(alias);
        if (sourceType == null) {
            throw new EqlStage1ProcessingException("Source type is required.");
        }

        this.sourceType = sourceType;
    }

    @Override
    public QueryBasedSource2 transform(final TransformatorToS2 resolver) {
        return (QueryBasedSource2) resolver.getTransformedSource(this);
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

        if (!(obj instanceof QrySource1BasedOnSyntheticType)) {
            return false;
        }

        final QrySource1BasedOnSyntheticType other = (QrySource1BasedOnSyntheticType) obj;
        if (!sourceType.equals(other.sourceType)) {
            return false;
        }

        return true;
    }
}