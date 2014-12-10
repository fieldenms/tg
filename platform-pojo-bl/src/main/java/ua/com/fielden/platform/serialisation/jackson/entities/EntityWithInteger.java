package ua.com.fielden.platform.serialisation.jackson.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.ResultOnly;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.GreaterOrEqual;
import ua.com.fielden.platform.entity.validation.annotation.Max;

/**
 * Entity class used for testing.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
public class EntityWithInteger extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    @Title(value = "Title", desc = "Desc")
    @ResultOnly
    private Integer prop;

    @Observable
    @Max(9999)
    @GreaterOrEqual(-600)
    public EntityWithInteger setProp(final Integer prop) {
        this.prop = prop;
        return this;
    }

    public Integer getProp() {
        return prop;
    }

}
