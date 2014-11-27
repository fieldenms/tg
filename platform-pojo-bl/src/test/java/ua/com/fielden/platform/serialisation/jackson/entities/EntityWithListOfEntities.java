package ua.com.fielden.platform.serialisation.jackson.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Entity class used for testing.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
public class EntityWithListOfEntities extends AbstractEntity<String> {

    @IsProperty(EntityWithListOfEntities.class)
    @Title(value = "Title", desc = "Desc")
    private final List<EntityWithListOfEntities> prop = new ArrayList<EntityWithListOfEntities>();

    @Observable
    public EntityWithListOfEntities setProp(final List<EntityWithListOfEntities> prop) {
        this.prop.clear();
        this.prop.addAll(prop);
        return this;
    }

    public List<EntityWithListOfEntities> getProp() {
        return Collections.unmodifiableList(prop);
    }

}
