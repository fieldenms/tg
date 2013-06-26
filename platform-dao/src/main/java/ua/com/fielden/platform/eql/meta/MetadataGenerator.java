package ua.com.fielden.platform.eql.meta;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.utils.EntityUtils.getRealProperties;

public class MetadataGenerator {

    public static final Map<Class<? extends AbstractEntity<?>>, EntityInfo> generate(final Set<Class<? extends AbstractEntity<?>>> entities) {
	final Map<Class<? extends AbstractEntity<?>>, EntityInfo> result = new HashMap<>();

	for (final Class<? extends AbstractEntity<?>> entityType : entities) {
	    result.put(entityType, new EntityInfo(entityType));
	}

	for (final EntityInfo entityInfo : result.values()) {
	    addProps(entityInfo, result);
	}

	return result;
    }

    private static void addProps(final EntityInfo entityInfo, final Map<Class<? extends AbstractEntity<?>>, EntityInfo> allEntitiesInfo) {



	for (final Field field : getRealProperties(entityInfo.javaType())) {
	    final Class javaType = determinePropertyType(entityInfo.javaType(), field.getName()); // redetermines prop type in platform understanding (e.g. type of Set<MeterReading> readings property will be MeterReading;

	    if (AbstractEntity.class.isAssignableFrom(entityInfo.getClass())){
		System.out.println(field.getName() + " as " + allEntitiesInfo.get(javaType/*field.getType()*/));
		entityInfo.getProps().put(field.getName(), new EntityTypePropInfo(field.getName(), entityInfo, allEntitiesInfo.get(javaType/*field.getType()*/), null));
	    } else {
		System.out.println(field.getName() + " as " + javaType/*field.getType()*/);
		entityInfo.getProps().put(field.getName(), new PrimTypePropInfo(field.getName(), entityInfo, javaType/*field.getType()*/, null));
	    }
//	    if (!result.containsKey(field.getName())) {
//		if (Collection.class.isAssignableFrom(field.getType()) && Finder.hasLinkProperty(entityType, field.getName())) {
//		    safeMapAdd(result, getCollectionalPropInfo(entityType, field));
//		} else if (field.isAnnotationPresent(Calculated.class)) {
//		    safeMapAdd(result, getCalculatedPropInfo(entityType, field));
//		} else if (field.isAnnotationPresent(MapTo.class)) {
//		    safeMapAdd(result, getCommonPropHibInfo(entityType, field));
//		} else if (Finder.isOne2One_association(entityType, field.getName())) {
//		    safeMapAdd(result, getOneToOnePropInfo(entityType, field));
//		} else if (!field.isAnnotationPresent(CritOnly.class)) {
//		    safeMapAdd(result, getSyntheticPropInfo(entityType, field));
//		} else {
//		    //System.out.println(" --------------------------------------------------------- " + entityType.getSimpleName() + ": " + field.getName());
//		}
//	    }
	}
    }
}
