package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Optional;

/**
 * Master entity object.
 *
 * @author TG Team
 *
 */
@KeyType(value = DynamicEntityKey.class)
@CompanionObject(ITgEntityTwoEntityKeys.class)
@MapEntityTo
public class TgEntityTwoEntityKeys extends AbstractPersistentEntity<DynamicEntityKey> {
    
    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    @Optional
    private TgEntityCompositeKey parentKey;
    
    @IsProperty
    @MapTo
    @CompositeKeyMember(2)
    @Optional
    private TgEntityStringKey entityKey;
    
    @Observable
    public TgEntityTwoEntityKeys setEntityKey(final TgEntityStringKey entityKey) {
        this.entityKey = entityKey;
        return this;
    }
    
    public TgEntityStringKey getEntityKey() {
        return entityKey;
    }
    
    @Observable
    public TgEntityTwoEntityKeys setParentKey(final TgEntityCompositeKey parentKey) {
        this.parentKey = parentKey;
        return this;
    }
    
    public TgEntityCompositeKey getParentKey() {
        return parentKey;
    }
    
}