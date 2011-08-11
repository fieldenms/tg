package ua.com.fielden.platform.reflection.asm.impl.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;

/**
 * A test entity, which gets enhanced with a new calculated property.
 * 
 * @author TG Team
 * 
 */
@KeyType(String.class)
public class EntityBeingEnhanced extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    private String prop1;

    public String getProp1() {
	return prop1;
    }

    @Observable
    public void setProp1(final String prop1) {
	this.prop1 = prop1;
    }

}
