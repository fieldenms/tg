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
@KeyType(TgEntityStringKey.class)
@KeyTitle("Key")
@CompanionObject(ITgEntityEntityKey.class)
@MapEntityTo
public class TgEntityEntityKey extends AbstractPersistentEntity<TgEntityStringKey> {
}