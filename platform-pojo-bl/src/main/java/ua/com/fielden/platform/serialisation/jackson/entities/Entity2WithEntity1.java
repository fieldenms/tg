package ua.com.fielden.platform.serialisation.jackson.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;

/**
 * Entity class used for testing.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@DescTitle("Description")
public class Entity2WithEntity1 extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    @Title(value = "Title", desc = "Desc")
    private Entity1WithEntity2 prop;

    @Observable
    public Entity2WithEntity1 setProp(final Entity1WithEntity2 prop) {
        this.prop = prop;
        return this;
    }

    public Entity1WithEntity2 getProp() {
        return prop;
    }

    @Override
    @Observable
    public Entity2WithEntity1 setDesc(String desc) {
        return super.setDesc(desc);
    }
}
