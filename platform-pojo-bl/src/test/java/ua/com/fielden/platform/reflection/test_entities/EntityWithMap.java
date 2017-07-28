package ua.com.fielden.platform.reflection.test_entities;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;

/**
 * Test class, which represent an entity derived directly from AbstractEntity with map property.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
public class EntityWithMap extends AbstractEntity<String> {

    @IsProperty(Object.class)
    private Map<String, Object> mapProperty = new HashMap<>();

    @Observable
    public EntityWithMap setMapProperty(final Map<String, Object> mapProperty) {
        this.mapProperty.clear();
        this.mapProperty.putAll(mapProperty);
        return this;
    }

    public Map<String, Object> getMapProperty() {
        return Collections.unmodifiableMap(mapProperty);
    }
}
