package ua.com.fielden.companion;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;

@CompanionObject(Entity2Co.class)
@KeyType(String.class)
@MapEntityTo
class Entity2 extends AbstractEntity<String> {
}
