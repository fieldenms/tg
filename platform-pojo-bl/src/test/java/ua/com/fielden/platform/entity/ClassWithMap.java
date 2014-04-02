package ua.com.fielden.platform.entity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for testing marshaling and unmarshaling of {@link AbstractEntity}s with property of this class (non-collection, non-AE-desccendant).
 * 
 * @author yura
 * 
 */
public class ClassWithMap {

    private final Map<String, Integer> mapProp = new HashMap<String, Integer>();

    protected ClassWithMap() {

    }

    public ClassWithMap(final Map<String, Integer> map) {
        mapProp.putAll(map);
    }

    public Map<String, Integer> getMapProp() {
        return Collections.unmodifiableMap(mapProp);
    }

}
