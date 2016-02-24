package ua.com.fielden.platform.entity.proxy;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;

@KeyType(String.class)
@CompanionObject(ITgEntityForProxy.class)
public class TgEntityForProxy extends AbstractEntity<String> {

    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @Title(value = "Entity", desc = "A property of an entity type")
    private TgEntityForProxy prop1;

    @Observable
    @EntityExists(TgEntityForProxy.class)
    public TgEntityForProxy setProp1(final TgEntityForProxy prop1) {
        this.prop1 = prop1;
        return this;
    }

    public TgEntityForProxy getProp1() {
        return prop1;
    }

    
}
