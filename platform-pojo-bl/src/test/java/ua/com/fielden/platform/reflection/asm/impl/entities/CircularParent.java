package ua.com.fielden.platform.reflection.asm.impl.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;

/**
 * A test entity, which gets modified by replacing the type of property <code>prop1</code>.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
public class CircularParent extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    private CircularChild prop1;

    public CircularChild getProp1() {
	return prop1;
    }

    @Observable
    public void setProp1(final CircularChild prop1) {
	this.prop1 = prop1;
    }

}
