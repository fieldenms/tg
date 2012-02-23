package ua.com.fielden.platform.entity.query;

import java.util.HashMap;
import java.util.Map;

public class ValueContainer {
    final ICompositeUserTypeInstantiate hibType;
    Map<String, Object> primitives = new HashMap<String, Object>();

    public ValueContainer(final ICompositeUserTypeInstantiate hibType) {
	this.hibType = hibType;
    }

    public Object instantiate() {
	return hibType.instantiate(primitives);
    }
}