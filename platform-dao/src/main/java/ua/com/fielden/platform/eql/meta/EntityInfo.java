package ua.com.fielden.platform.eql.meta;

import java.util.SortedMap;
import java.util.TreeMap;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

public class EntityInfo {
    private final Class<? extends AbstractEntity<?>> javaType;
    private final SortedMap<String, AbstractPropInfo> props = new TreeMap<>();

    public EntityInfo(final Class<? extends AbstractEntity<?>> javaType) {
	this.javaType = javaType;
    }

    public Object resolve(final String dotNotatedPropName) {
	final Pair<String, String> parts = EntityUtils.splitPropByFirstDot(dotNotatedPropName);
	final AbstractPropInfo foundPart = props.get(parts.getKey());

	if (foundPart != null) {

//	    if (foundPart instanceof PrimTypePropInfo) {
//		return foundPart;
//	    }
//	    return parts.getValue() != null ? ((EntityTypePropInfo) foundPart).getPropEntityInfo().resolve(parts.getValue()) : foundPart;
	    return foundPart.resolve(parts.getValue());
	} else {
	    return null;
	}
    }

    protected SortedMap<String, AbstractPropInfo> getProps() {
	return props;
    }

    protected Class<? extends AbstractEntity<?>> getJavaType() {
        return javaType;
    }

    @Override
    public String toString() {
	return javaType.getSimpleName();
    }
}