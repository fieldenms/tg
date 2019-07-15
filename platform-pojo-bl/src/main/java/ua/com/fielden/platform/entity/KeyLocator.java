package ua.com.fielden.platform.entity;

import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@KeyType(NoKey.class)
public class KeyLocator<T extends AbstractEntity<?>> extends AbstractFunctionalEntityWithCentreContext<NoKey> {

    @IsProperty
    @Title("Entity Key")
    private String entityKey;

    @IsProperty
    @Title("Entity")
    private T entity;

    private final Class<T> entityTypeAsClass;

    protected KeyLocator(final Class<T> entityTypeAsClass) {
        setKey(NO_KEY);
        this.entityTypeAsClass = entityTypeAsClass;
    }

    @Observable
    public KeyLocator<T> setEntity(final T entity) {
        this.entity = entity;
        return this;
    }

    public T getEntity() {
        return entity;
    }

    @Observable
    public KeyLocator<T> setEntityKey(final String entityKey) {
        this.entityKey = entityKey;
        return this;
    }

    public String getEntityKey() {
        return entityKey;
    }

    public Class<T> getEntityTypeAsClass() {
        return entityTypeAsClass;
    }
}
