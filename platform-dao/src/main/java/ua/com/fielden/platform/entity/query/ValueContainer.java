package ua.com.fielden.platform.entity.query;

import java.util.Map;

public class ValueContainer {
    final ICompositeUserTypeInstantiate hibType;
    final Map<String, Object> primitives;

    public ValueContainer(final ICompositeUserTypeInstantiate hibType, final Map<String, Object> primitives) {
        this.hibType = hibType;
        this.primitives = primitives;
    }

    public ICompositeUserTypeInstantiate getHibType() {
        return hibType;
    }

    public Map<String, Object> getPrimitives() {
        return primitives;
    }
}