package ua.com.fielden.platform.serialisation.jackson.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;

/**
 * Entity class used for testing.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@MapEntityTo
@CompanionObject(IOtherEntity.class)
public class OtherEntity extends AbstractEntity<String> {

}
