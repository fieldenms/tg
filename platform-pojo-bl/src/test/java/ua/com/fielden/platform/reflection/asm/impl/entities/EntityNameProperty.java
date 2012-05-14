package ua.com.fielden.platform.reflection.asm.impl.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.KeyType;

/**
 * A test entity, which has a name that starts with a name a of type that has property of this type.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
public class EntityNameProperty extends AbstractEntity<String> {

}
