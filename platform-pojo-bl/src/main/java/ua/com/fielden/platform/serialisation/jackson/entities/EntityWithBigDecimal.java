package ua.com.fielden.platform.serialisation.jackson.entities;

import java.math.BigDecimal;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Entity class used for testing.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
public class EntityWithBigDecimal extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    @Title(value = "Title", desc = "Desc")
    private BigDecimal prop;

    @Observable
    public EntityWithBigDecimal setProp(final BigDecimal prop) {
        this.prop = prop;
        return this;
    }

    public BigDecimal getProp() {
        return prop;
    }

}
