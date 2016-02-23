package ua.com.fielden.platform.entity.proxy.old;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;

@KeyType(String.class)
public class OwnerEntity extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    @Title(value = "Entity", desc = "A property of an entity type")
    private EntityForProxy prop1;

    @Observable
    @EntityExists(EntityForProxy.class)
    public OwnerEntity setProp1(final EntityForProxy prop1) {
        this.prop1 = prop1;
        return this;
    }

    public EntityForProxy getProp1() {
        return prop1;
    }

    

    
}
