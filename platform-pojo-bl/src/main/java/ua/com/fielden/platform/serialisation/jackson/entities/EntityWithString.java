package ua.com.fielden.platform.serialisation.jackson.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.UpperCase;
import ua.com.fielden.platform.entity.validation.annotation.Max;

/**
 * Entity class used for testing.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Special Key", desc = "Special Key desc")
@DescTitle(value = "Special Desc", desc = "Special Desc desc")
public class EntityWithString extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    @Title(value = "Special Prop", desc = "Special Prop desc")
    @UpperCase
    private String prop;

    @Observable
    @Max(255)
    public EntityWithString setProp(final String prop) {
        this.prop = prop;
        return this;
    }

    public String getProp() {
        return prop;
    }

}
