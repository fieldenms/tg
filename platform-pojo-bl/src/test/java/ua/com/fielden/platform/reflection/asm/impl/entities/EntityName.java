package ua.com.fielden.platform.reflection.asm.impl.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * A test entity, which has a name and a property with type that starts with the same name. This entity is used to test type modification where one of its properties has a type
 * name that starts with the same name as the type being modified.
 * 
 * @author TG Team
 * 
 */
@KeyType(String.class)
public class EntityName extends AbstractEntity<String> {
    @IsProperty
    @MapTo
    @Title(value = "Property", desc = "Desc")
    private EntityNameProperty prop;

    @Observable
    public EntityName setProp(final EntityNameProperty prop) {
        this.prop = prop;
        return this;
    }

    public EntityNameProperty getProp() {
        return prop;
    }

}
