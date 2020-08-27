package ua.com.fielden.platform.sample.domain;

import java.math.BigDecimal;

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
@KeyType(BigDecimal.class)
@KeyTitle("Key")
@CompanionObject(ITgEntityBigDecimalKey.class)
@MapEntityTo
public class TgEntityBigDecimalKey extends AbstractPersistentEntity<BigDecimal> {
}