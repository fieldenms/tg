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
@CompanionObject(ITgEntityCompositeBooleanKey.class)
@MapEntityTo
public class TgEntityCompositeBooleanKey extends AbstractPersistentEntity<DynamicEntityKey> {
    
    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    @Optional
    private boolean booleanKey;
    
    @Observable
    public TgEntityCompositeBooleanKey setBooleanKey(final boolean booleanKey) {
        this.booleanKey = booleanKey;
        return this;
    }
    
    public boolean getBooleanKey() {
        return booleanKey;
    }
    
}