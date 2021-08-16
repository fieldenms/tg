package ua.com.fielden.platform.serialisation.jackson.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
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
@DescTitle("Description")
public class EntityWithBoolean extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    @Title(value = "Title", desc = "Desc")
    private boolean prop = false;

    @Observable
    public EntityWithBoolean setProp(final boolean prop) {
        this.prop = prop;
        return this;
    }

    public boolean isProp() {
        return prop;
    }

}
