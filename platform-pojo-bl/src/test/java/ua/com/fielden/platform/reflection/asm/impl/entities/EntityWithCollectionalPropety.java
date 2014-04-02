package ua.com.fielden.platform.reflection.asm.impl.entities;

import java.util.Collection;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;

/**
 * A test entity with collectional property
 * 
 * @author TG Team
 * 
 */
@KeyType(String.class)
public class EntityWithCollectionalPropety extends AbstractEntity<String> {

    @IsProperty(EntityBeingEnhanced.class)
    private Collection<EntityBeingEnhanced> prop1;

    public Collection<EntityBeingEnhanced> getProp1() {
        return prop1;
    }

    @Observable
    public void setProp1(final Collection<EntityBeingEnhanced> prop1) {
        this.prop1 = prop1;
    }

    @Observable
    public void addToProp1(final EntityBeingEnhanced value) {
        if (prop1 != null) {
            prop1.add(value);
        }
    }

    @Observable
    public void removeFromProp1(final EntityBeingEnhanced value) {
        if (prop1 != null) {
            prop1.remove(value);
        }
    }
}
