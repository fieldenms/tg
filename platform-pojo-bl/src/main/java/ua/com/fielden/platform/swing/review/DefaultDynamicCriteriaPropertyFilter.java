package ua.com.fielden.platform.swing.review;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.Ignore;
import ua.com.fielden.platform.entity.annotation.Invisible;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.treemodel.IPropertyFilter;

/**
 * This is default implementation of the property filter used for both trees on the dynamic criteria wizard to represent criteria properties and resultant properties.
 *
 * Any custom filters for these tree should implement this default implementation.
 *
 * @author TG Team
 *
 */
public class DefaultDynamicCriteriaPropertyFilter implements IPropertyFilter {

    @Override
    public boolean shouldExcludeProperty(final Class<?> ownerType, final Field property) {
	final Class<?> propertyType = property.getType();
	if ((AbstractEntity.class.isAssignableFrom(propertyType)) && (Modifier.isAbstract(propertyType.getModifiers()) || !AnnotationReflector.isAnnotationPresent(KeyType.class, propertyType))) {
	    return true;
	}
	if (Enum.class.isAssignableFrom(propertyType) //
//		|| Collection.class.isAssignableFrom(propertyType)//
		|| ("key".equals(property.getName()) && (!AnnotationReflector.isAnnotationPresent(KeyTitle.class, ownerType) || !AbstractEntity.class.isAssignableFrom(AnnotationReflector.getKeyType(ownerType))))//
		|| ("desc".equals(property.getName()) && !AnnotationReflector.isAnnotationPresent(DescTitle.class, ownerType))//
		|| property.isAnnotationPresent(Invisible.class) //
		|| property.isAnnotationPresent(Ignore.class)) {
	    return true;
	}

	return false;
    }

    @Override
    public boolean shouldBuildChildrenFor(final Class<?> ownerType, final Field property) {
	if (AbstractEntity.class.isAssignableFrom(property.getType()) && property.isAnnotationPresent(CritOnly.class)) {
	    return false;
	}
	return true;
    }

}
