package ua.com.fielden.platform.basic.autocompleter;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;

/**
 * This is a fall back implementation for {@link IValueMatcherWithContext} matchers for {@link PropertyDescriptor}s, which does not do anything with the provided context.
 * It simply performs the search by key operation.
 * <p>
 * Suitable for entity master's matchers.
 * 
 * @author TG Team
 *
 * @param <CONTEXT> -- context type
 * @param <T> -- enclosing entity type
 */
public class FallbackPropertyDescriptorMatcherWithContext<CONTEXT extends AbstractEntity<?>, T extends AbstractEntity<?>> extends AbstractSearchPropertyDescriptorByKeyWithContext<CONTEXT, T> {

    /**
     * Creates fallback matcher for {@link PropertyDescriptor}s using enclosing entity type.
     * 
     * @param enclosingEntityType
     */
    public FallbackPropertyDescriptorMatcherWithContext(final Class<T> enclosingEntityType) {
        super(enclosingEntityType);
    }

}