package ua.com.fielden.platform.basic.autocompleter;

import ua.com.fielden.platform.basic.IValueMatcherWithCentreContext;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * Key-based value matcher for {@link PropertyDescriptor}s, which supports centre context assignment.
 * <p>
 * Suitable for entity centre's selection criteria matchers.
 * 
 * @author TG Team
 *
 * @param <T> -- enclosing entity type
 */
public abstract class AbstractSearchPropertyDescriptorByKeyWithCentreContext<T extends AbstractEntity<?>> extends AbstractSearchPropertyDescriptorByKey<T> implements IValueMatcherWithCentreContext<PropertyDescriptor<T>> {
    private CentreContext<PropertyDescriptor<T>, ?> context;

    /**
     * Creates matcher for {@link PropertyDescriptor}s using enclosing entity type.
     * 
     * @param enclosingEntityType
     */
    public AbstractSearchPropertyDescriptorByKeyWithCentreContext(final Class<T> enclosingEntityType) {
        super(enclosingEntityType);
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