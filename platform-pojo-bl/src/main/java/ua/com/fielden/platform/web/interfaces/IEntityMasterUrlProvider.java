package ua.com.fielden.platform.web.interfaces;

import ua.com.fielden.platform.entity.AbstractEntity;

import java.util.Optional;

/**
 * A contract to generate an entity master URI for a specific entity.
 *
 * @author TG Team
 */
@FunctionalInterface
public interface IEntityMasterUrlProvider {

    static String PARTIAL_URL_PATTERN = "#/master/%s/%s";

    /**
     * Generates an entity master URI for {@code entity}. It might return empty URI string if {@code entity} does not have a registered entity master.
     *
     * @param entity
     * @return
     */
    Optional<String> masterUrlFor(final AbstractEntity<?> entity);

}
