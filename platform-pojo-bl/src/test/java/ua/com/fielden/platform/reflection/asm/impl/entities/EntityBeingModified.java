package ua.com.fielden.platform.reflection.asm.impl.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;

/**
 * A test entity with two properties of the same type, which gets enhanced and one property of self-type.
 * 
 * @author TG Team
 * 
 */
@KeyType(String.class)
public class EntityBeingModified extends AbstractEntity<String> {

    @IsProperty
    private EntityBeingEnhanced prop1;

    @IsProperty
    private EntityBeingEnhanced prop2;

    @IsProperty
    private EntityBeingModified selfTypeProperty;

    public EntityBeingEnhanced getProp1() {
	return prop1;
    }

    @Observable
    public void setProp1(final EntityBeingEnhanced prop1) {
	this.prop1 = prop1;
    }

    public EntityBeingEnhanced getProp2() {
	return prop2;
    }

    @Observable
    public void setProp2(final EntityBeingEnhanced prop2) {
	this.prop2 = prop2;
    }

    public EntityBeingModified getSelfTypeProperty() {
	return selfTypeProperty;
    }

    @Observable
    public void setSelfTypeProperty(final EntityBeingModified selfTypeProperty) {
	this.selfTypeProperty = selfTypeProperty;
    }
}
