/**
 *
 */
package ua.com.fielden.platform.basic.autocompleter;

import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.reflection.Finder.getPropertyDescriptors;

import java.lang.reflect.Field;
import java.util.List;

import ua.com.fielden.platform.basic.IValueMatcherWithCentreContext;
import ua.com.fielden.platform.basic.IValueMatcherWithFetch;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * Key-based value matcher for {@link PropertyDescriptor}s, which supports entity centre context assignment.
 * 
 * @author TG Team
 *
 * @param <T> -- enclosing entity type
 */
public abstract class AbstractSearchPropertyDescriptorByKeyWithCentreContext<T extends AbstractEntity<?>> implements IValueMatcherWithCentreContext<PropertyDescriptor<T>>, IValueMatcherWithFetch<PropertyDescriptor<T>> {
    private final Class<T> enclosingEntityType;
    private CentreContext<PropertyDescriptor<T>, ?> context;

    /**
     * Creates matcher for {@link PropertyDescriptor}s using enclosing entity type.
     * 
     * @param enclosingEntityType
     */
    public AbstractSearchPropertyDescriptorByKeyWithCentreContext(final Class<T> enclosingEntityType) {
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
     * Specifies custom property field exclusion logic when building property descriptors to be matched against.
     * <p>
     * Override this method to provide custom behaviour.
     *  
     * @param field
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
     * @param searchString
     * @return
     */
    protected List<PropertyDescriptor<T>> findPropertyDescriptorMatches(final String searchString) {
        final List<PropertyDescriptor<T>> allPropertyDescriptors = getPropertyDescriptors(enclosingEntityType, this::shouldSkip);
        allPropertyDescriptors.sort(null); // let's represent the matching property in alphabetic order
        return new PojoValueMatcher<>(allPropertyDescriptors, KEY, allPropertyDescriptors.size()).findMatches(searchString);
    }

    @Override
    public fetch<PropertyDescriptor<T>> getFetch() {
        throw new UnsupportedOperationException("Fetch model should not be used for property descriptors retrieval.");
    }

    @Override
    public void setFetch(final fetch<PropertyDescriptor<T>> fetchModel) {
        throw new UnsupportedOperationException("Fetch model should not be used for property descriptors retrieval.");
    }

    @Override
    public CentreContext<PropertyDescriptor<T>, ?> getContext() {
        return context;
    }

    @Override
    public AbstractSearchPropertyDescriptorByKeyWithCentreContext<T> setContext(final CentreContext<PropertyDescriptor<T>, ?> context) {
        this.context = context;
        return this;
    }

}