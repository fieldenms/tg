package ua.com.fielden.platform.serialisation.jackson.entities;

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
public class Entity1WithEntity2 extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    @Title(value = "Title", desc = "Desc")
    private Entity2WithEntity1 prop;

    @Observable
    public Entity1WithEntity2 setProp(final Entity2WithEntity1 prop) {
        this.prop = prop;
        return this;
    }

    public Entity2WithEntity1 getProp() {
        return prop;
    }

}
