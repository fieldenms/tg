package ua.com.fielden.platform.eql.stage1.sources;

import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.TransformationContext1;
import ua.com.fielden.platform.eql.stage2.sources.Source2BasedOnPersistentType;

public class Source1BasedOnPersistentType extends AbstractSource1<Source2BasedOnPersistentType> {

    public Source1BasedOnPersistentType(final Class<? extends AbstractEntity<?>> sourceType, final String alias, final Integer id) {
        super(sourceType, alias, id);
    }

    @Override
    public Source2BasedOnPersistentType transform(final TransformationContext1 context) {
        return new Source2BasedOnPersistentType(context.querySourceInfoProvider.getModelledQuerySourceInfo(sourceType()), alias, id);
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return Set.of(sourceType());
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        return prime * result + Source1BasedOnPersistentType.class.getName().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof Source1BasedOnPersistentType;
    }
}