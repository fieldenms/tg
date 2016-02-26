package ua.com.fielden.platform.basic;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 *
 * A value matcher contract that required an execution context that is specific to an Entity Centre.
 * The fulfilment of a centre context depends on its configuration, which is provided as part of Entity Centre definition.
 * In the least case it should provide an instance of {@link EntityQueryCriteria} that represent the Entity Centre.
 * <p>
 * The actual implementation of concrete value matchers is responsible for correctly using the provided context.
 *
 * @author TG Team
 *
 * @param <T> -- the type of autocompleted values
 */
public interface IValueMatcherWithCentreContext<T extends AbstractEntity<?>> extends IValueMatcherWithFetch<T> {

    /**
     * Sets execution context. Value matcher throws an exception if the context is not provided.
     *
     * @param context
     */
    void setContext(final CentreContext<T, ?> context);

    /**
     * Provides access to the context, which is needed in order to use it in custom autocompletion logic.
     *
     * @return
     */
    CentreContext<T, ?> getContext();
}
