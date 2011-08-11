package ua.com.fielden.platform.reflection.asm.impl.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;

/**
 * A test entity representing a top level type with two properties of the same type, which gets modified.
 * 
 * @author TG Team
 * 
 */
@KeyType(String.class)
public class TopLevelEntity extends AbstractEntity<String> {

    @IsProperty
    private EntityBeingModified prop1;

    @IsProperty
    private EntityBeingModified prop2;

    public EntityBeingModified getProp1() {
	return prop1;
    }

    @Observable
    public void setProp1(final EntityBeingModified prop1) {
	this.prop1 = prop1;
    }

    public EntityBeingModified getProp2() {
	return prop2;
    }

    @Observable
    public void setProp2(final EntityBeingModified prop2) {
	this.prop2 = prop2;
    }
}
