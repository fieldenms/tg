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
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.treemodel.IPropertyFilter;

/**
 * This is default implementation of the property filter used by {@link ModelBasedEntityQueryCriteria}.
 *
 * @author TG Team
 *
 */
public final class DefaultModelBasedCriteriaPropertyFilter implements IPropertyFilter {

    @Override
    public boolean shouldExcludeProperty(final Class<?> ownerType, final Field property) {
	final Class<?> propertyType = property.getType();
	if ((AbstractEntity.class.isAssignableFrom(propertyType))
		&& (Modifier.isAbstract(propertyType.getModifiers()) || !AnnotationReflector.isAnnotationPresent(KeyType.class, propertyType))) {
	    return true;
	}
	if (Enum.class.isAssignableFrom(propertyType) //
		|| ("key".equals(property.getName()) && (Finder.getKeyMembers(ownerType).size() > 1))
		|| ("key".equals(property.getName()) && (!AnnotationReflector.isAnnotationPresent(KeyTitle.class, ownerType)))//
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
