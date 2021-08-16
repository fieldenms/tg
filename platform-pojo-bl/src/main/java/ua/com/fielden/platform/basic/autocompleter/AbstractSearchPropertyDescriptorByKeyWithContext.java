package ua.com.fielden.platform.basic.autocompleter;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;

/**
 * Key-based value matcher for {@link PropertyDescriptor}s, which supports context assignment.
 * <p>
 * Suitable for entity master's matchers.
 * 
 * @author TG Team
 *
 * @param <CONTEXT> -- context type
 * @param <T> -- enclosing entity type
 */
public abstract class AbstractSearchPropertyDescriptorByKeyWithContext<CONTEXT extends AbstractEntity<?>, T extends AbstractEntity<?>> extends AbstractSearchPropertyDescriptorByKey<T> implements IValueMatcherWithContext<CONTEXT, PropertyDescriptor<T>> {
    private CONTEXT context;

    /**
     * Creates matcher for {@link PropertyDescriptor}s using enclosing entity type.
     * 
     * @param enclosingEntityType
     */
    public AbstractSearchPropertyDescriptorByKeyWithContext(final Class<T> enclosingEntityType) {
        super(enclosingEntityType);
    }

    @Override
    public CONTEXT getContext() {
        return context;
    }

    @Override
    public void setContext(final CONTEXT context) {
        this.context = context;
    }

}