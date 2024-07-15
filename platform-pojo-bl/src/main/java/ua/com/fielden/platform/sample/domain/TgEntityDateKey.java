package ua.com.fielden.platform.sample.domain;

import java.util.Date;

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
@KeyType(Date.class)
@KeyTitle("Key")
@CompanionObject(ITgEntityDateKey.class)
@MapEntityTo
public class TgEntityDateKey extends AbstractPersistentEntity<Date> {
}