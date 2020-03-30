package ua.com.fielden.platform.eql.meta.model;

import java.util.HashMap;
import java.util.Map;

public class Entity {
    final Class<?> javaType;
    final Map<String, IProp> props = new HashMap<>();
    
    public Entity(final Class<?> javaType) {
        this.javaType = javaType;
    }
    
    public void add(final IProp prop) {
        props.put(prop.name(), prop);
    }
}
