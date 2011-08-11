package ua.com.fielden.platform.treemodel;

import java.lang.reflect.Field;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Predicate that defines which properties of the {@link AbstractEntity} must be filtered out or not
 * 
 * @author oleh
 * 
 */
public interface IPropertyFilter {

    /**
     * Returns true if the property {@code type} doen't satisfy the condition defined by this method, otherwise returns false.
     * 
     * @param type
     * @return
     */
    boolean shouldExcludeProperty(final Class<?> ownerType, Field property);

    /**
     * Returns true if children of the specified property should be build. otherwise it returns false.
     * 
     * @param ownerType
     * @param property
     * @return
     */
    boolean shouldBuildChildrenFor(final Class<?> ownerType, Field property);
}
