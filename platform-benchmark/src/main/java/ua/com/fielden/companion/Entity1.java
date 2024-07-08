package ua.com.fielden.companion;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;

@CompanionObject(Entity1Co.class)
@KeyType(String.class)
@MapEntityTo
class Entity1 extends AbstractEntity<String> {
}
