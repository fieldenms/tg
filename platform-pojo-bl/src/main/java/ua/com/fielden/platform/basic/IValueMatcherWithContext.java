package ua.com.fielden.platform.basic;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 *
 * A value matcher contract that required an execution context. The context is always some kind of entity.
 * In most cases it would most likely be the entity owning the property for which the autocompletion is required.
 * <p>
 * The actual implementation of concrete value matchers is responsible for correctly using the provided context.
 * <p>
 * An interesting use case is where context is represented by a persistent entity and the matching logic depends on both the current (potentially not yet persisted) context and
 * on its persisted version. A concrete matcher implementation would be able to retrieve the persisted version by using an ID of the provided current context version.
 *
 * @author TG Entity
 *
 * @param <CONTEXT> -- the type of execution context
 * @param <T> -- the type of autocompleted values
 */
public interface IValueMatcherWithContext<CONTEXT extends AbstractEntity<?>, T extends AbstractEntity<?>> extends IValueMatcherWithFetch<T> {

    /**
     * Sets execution context. Value matcher throws an exception if the context is not provided.
     *
     * @param context
     */
    void setContext(final CONTEXT context);

    /**
     * Provides access to the context, which is needed in order to use it in custom autocompletion logic.
     *
     * @return
     */
    CONTEXT getContext();
}
