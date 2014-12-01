package ua.com.fielden.platform.serialisation.jackson.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;

/**
 * Entity class used for testing.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
public class Entity2WithEntity1 extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    @Title(value = "Title", desc = "Desc")
    private Entity1WithEntity2 prop;

    @Observable
    @EntityExists(OtherEntity.class)
    public Entity2WithEntity1 setProp(final Entity1WithEntity2 prop) {
        this.prop = prop;
        return this;
    }

    public Entity1WithEntity2 getProp() {
        return prop;
    }

}
