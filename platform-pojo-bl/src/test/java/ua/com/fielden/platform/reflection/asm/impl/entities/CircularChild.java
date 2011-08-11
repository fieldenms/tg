package ua.com.fielden.platform.reflection.asm.impl.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;

/**
 * A test entity, which gets enhanced with a new calculated property and which contains a property of a circular type.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
public class CircularChild extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    private CircularParent prop1;

    public CircularParent getProp1() {
	return prop1;
    }

    @Observable
    public void setProp1(final CircularParent prop1) {
	this.prop1 = prop1;
    }

}
