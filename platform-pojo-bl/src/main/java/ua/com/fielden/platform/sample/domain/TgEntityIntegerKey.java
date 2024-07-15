package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;

/**
 * Master entity object.
 *
 * @author TG Team
 *
 */
@KeyType(Integer.class)
@KeyTitle("Key")
@CompanionObject(ITgEntityIntegerKey.class)
@MapEntityTo
public class TgEntityIntegerKey extends AbstractPersistentEntity<Integer> {
}