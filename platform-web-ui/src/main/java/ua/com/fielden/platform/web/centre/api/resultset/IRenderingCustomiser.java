package ua.com.fielden.platform.web.centre.api.resultset;

import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract that is used by Entity Centre DSL specifying custom rendering logic.
 * It is envisaged that a single instance of this contract should handle computation of rendering hints for both an entity and its properties.
 * <p>
 * The actual form or structure of rendering hints is very much rendering engine specific.
 * Therefore, the contract uses type parameter <code>R</code> to represent the type of such structure in the most flexible for the end developer way.
 * <p>
 * This contract should be view as a function that returns rendering hints for a single passed as an argument entity.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IRenderingCustomiser<T extends AbstractEntity<?>, R> {

    /**
     * Accepts an entity instance and computes rendering hints for its representation, and representation of its properties.
     * <p>
     * There could be a situation where an entity instance does not require customisation of its rendering.
     * In such cases an empty {@link Optional} instance should be returned, which is the default implementation of this contract.
     *
     * @param entity
     */
    default Optional<R> getCustomRenderingFor(final T entity) {
        return Optional.empty();
    }
}
