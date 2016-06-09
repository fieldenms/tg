package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract that is used by Entity Centre DSL for specifying a custom logic to assigned values to custom properties that were defined
 * during entity centre specification.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface ICustomPropsAssignmentHandler<T extends AbstractEntity<?>> {

    /**
     * Accepts an entity instance and assigns values to custom properties of this entity.
     * Of course, there is no restriction on changing only the custom properties, and it could also apply modification of other entity properties
     * if for some reason this becomes required.
     *
     * @param entity
     */
    void assignValues(final T entity);
}
