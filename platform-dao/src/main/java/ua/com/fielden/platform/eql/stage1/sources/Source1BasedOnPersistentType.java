package ua.com.fielden.platform.eql.stage1.sources;

import java.util.Objects;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.TransformationContext;
import ua.com.fielden.platform.eql.stage2.sources.Source2BasedOnPersistentType;

public class Source1BasedOnPersistentType extends AbstractSource1<Source2BasedOnPersistentType> {
    private final Class<? extends AbstractEntity<?>> sourceType;

    public Source1BasedOnPersistentType(final Class<? extends AbstractEntity<?>> sourceType, final String alias, final int id) {
        super(alias, id);
        this.sourceType = Objects.requireNonNull(sourceType);
    }

    @Override
    public Class<? extends AbstractEntity<?>> sourceType() {
        return sourceType;
    }

    @Override
    public Source2BasedOnPersistentType transform(final TransformationContext context) {
        return new Source2BasedOnPersistentType(sourceType(), context.domainInfo.getEntityInfo(sourceType()), alias, transformId(context));
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

        if (!(obj instanceof Source1BasedOnPersistentType)) {
            return false;
        }

        final Source1BasedOnPersistentType other = (Source1BasedOnPersistentType) obj;

        return Objects.equals(sourceType, other.sourceType);
    }
}