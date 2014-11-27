package ua.com.fielden.platform.serialisation.jackson.entities;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

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
public class EntityWithSetOfEntities extends AbstractEntity<String> {

    @IsProperty(EntityWithSetOfEntities.class)
    @Title(value = "Title", desc = "Desc")
    private final Set<EntityWithSetOfEntities> prop = new LinkedHashSet<EntityWithSetOfEntities>();

    @Observable
    public EntityWithSetOfEntities setProp(final Set<EntityWithSetOfEntities> prop) {
        this.prop.clear();
        this.prop.addAll(prop);
        return this;
    }

    public Set<EntityWithSetOfEntities> getProp() {
        return Collections.unmodifiableSet(prop);
    }

}
