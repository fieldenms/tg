package ua.com.fielden.platform.basic.autocompleter;

import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.reflection.Finder.getPropertyDescriptors;

import java.lang.reflect.Field;
import java.util.List;

import ua.com.fielden.platform.basic.IValueMatcherWithFetch;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.reflection.Finder;

/**
 * Abstract key-based value matcher implementation for {@link PropertyDescriptor}s. Suitable for both centre and master matchers.
 * 
 * @author TG Team
 *
 * @param <T> -- enclosing entity type
 */
abstract class AbstractSearchPropertyDescriptorByKey<T extends AbstractEntity<?>> implements IValueMatcherWithFetch<PropertyDescriptor<T>> {
    private static final String ERR_FETCH_MODEL_NOT_APPLICABLE = "No fetch model should be used to retrieve property descriptors.";
    private final Class<T> enclosingEntityType;

    /**
     * Creates matcher for {@link PropertyDescriptor}s using an enclosing entity type.
     * 
     * @param enclosingEntityType  an entity for which property definitions are obtained.
     */
    public AbstractSearchPropertyDescriptorByKey(final Class<T> enclosingEntityType) {
        this.enclosingEntityType = enclosingEntityType;
    }

    @Override
    public List<PropertyDescriptor<T>> findMatches(final String searchString) {
        return findPropertyDescriptorMatches(searchString);
    }

    @Override
    public List<PropertyDescriptor<T>> findMatchesWithModel(final String searchString, final int dataPage) {
        return findPropertyDescriptorMatches(searchString);
    }
    
    /**
     * A predicate to exclude properties from the matcher considerations.
     * This predicated is passed into {@link Finder#getPropertyDescriptors(Class, java.util.function.Predicate)} in method {@link #findPropertyDescriptorMatches(String)}.
     * <p>
     * No properties are excluded by default. 
     * Override this method to provide a domain-specific logic for excluding properties from the matcher consideration.
     *  
     * @param field a field representing a property, which should be skipped.
     */
    protected boolean shouldSkip(final Field field) {
        return false;
    }

    /**
     * Finds property descriptors matching to {@code searchString}.
     * By default, this method uses {@link #shouldSkip(Field)} to filter out some property descriptors.
     * <p>
     * Override this method to provide fully custom behaviour.
     * 
     * @param searchString  a searching string used for matching properties.
     * @return
     */
    protected List<PropertyDescriptor<T>> findPropertyDescriptorMatches(final String searchString) {
        final List<PropertyDescriptor<T>> allPropertyDescriptors = getPropertyDescriptors(enclosingEntityType, this::shouldSkip);
        allPropertyDescriptors.sort(null); // let's represent the matching property in alphabetic order
        return new PojoValueMatcher<>(allPropertyDescriptors, KEY, allPropertyDescriptors.size()).findMatches(searchString);
    }

    @Override
    public fetch<PropertyDescriptor<T>> getFetch() {
        throw new UnsupportedOperationException(ERR_FETCH_MODEL_NOT_APPLICABLE);
    }

    @Override
    public void setFetch(final fetch<PropertyDescriptor<T>> fetchModel) {
        throw new UnsupportedOperationException(ERR_FETCH_MODEL_NOT_APPLICABLE);
    }

}