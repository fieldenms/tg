package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;

/**
 * Master entity object.
 *
 * @author TG Team
 *
 */
@KeyType(value = DynamicEntityKey.class)
@KeyTitle(value = "Entity With Composite Boolean Key", desc = "Represents an entity with single composite key of type Boolean.")
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