package ua.com.fielden.platform.web.interfaces;

import ua.com.fielden.platform.entity.AbstractEntity;

import java.util.Optional;

/**
 * A contract to generate an entity master URI for a specific entity.
 *
 * @author TG Team
 */
public interface IEntityMasterUnifiedResourceLocator {

    /**
     * Generates an entity master URI for {@code entity}. It might return empty URI string if {@code entity} does not have a registered entity master.
     *
     * @param entity
     * @return
     * @param <T>
     */
    <T extends AbstractEntity<?>> Optional<String> masterUrlFor(final T entity);

}
