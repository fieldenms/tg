package ua.com.fielden.platform.serialisation.jackson.entities;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

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
public class EntityWithMapOfEntities extends AbstractEntity<String> {

    @IsProperty(EntityWithMapOfEntities.class)
    @Title(value = "Title", desc = "Desc")
    private final Map<String, EntityWithMapOfEntities> prop = new LinkedHashMap<>();

    @Observable
    public EntityWithMapOfEntities setProp(final Map<String, EntityWithMapOfEntities> prop) {
        this.prop.clear();
        this.prop.putAll(prop);
        return this;
    }

    public Map<String, EntityWithMapOfEntities> getProp() {
        return Collections.unmodifiableMap(prop);
    }

}
