package ua.com.fielden.platform.eql.meta.result;

import ua.com.fielden.platform.entity.AbstractEntity;

public class EqlQueryResultItemForPersistentEntityType<T extends AbstractEntity<?>> extends AbstractEqlQueryResultItem<T> implements IEqlQueryResultItem<T> {
    
    public EqlQueryResultItemForPersistentEntityType(final String name, final Class<T> javaType) {
        super(name, javaType);
    }

    @Override
    public EqlPropResolutionProgress resolve(final EqlPropResolutionProgress resolutionProgress) {
        return resolutionProgress;
    }
}
