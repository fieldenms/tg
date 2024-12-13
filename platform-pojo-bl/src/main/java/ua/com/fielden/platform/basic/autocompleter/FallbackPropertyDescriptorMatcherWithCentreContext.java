package ua.com.fielden.platform.basic.autocompleter;

import ua.com.fielden.platform.basic.IValueMatcherWithCentreContext;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;

/**
 * This is a fallback implementation for {@link IValueMatcherWithCentreContext} selection criteria matchers for {@link PropertyDescriptor}s, which does not do anything with the provided context.
 * It simply performs the search-by-key operation.
 * <p>
 * Suitable for entity centre's selection criteria matchers.
 * 
 * @author TG Team
 *
 * @param <T> -- enclosing entity type
 */
public class FallbackPropertyDescriptorMatcherWithCentreContext<T extends AbstractEntity<?>> extends AbstractSearchPropertyDescriptorByKeyWithCentreContext<T> {

    /**
     * Creates fallback matcher for {@link PropertyDescriptor}s using an enclosing entity type.
     * 
     * @param enclosingEntityType an entity for which property definitions are obtained.
     */
    public FallbackPropertyDescriptorMatcherWithCentreContext(final Class<T> enclosingEntityType) {
        super(enclosingEntityType);
    }

}