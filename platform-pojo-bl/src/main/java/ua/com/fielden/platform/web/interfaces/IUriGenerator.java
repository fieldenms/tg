package ua.com.fielden.platform.web.interfaces;

import ua.com.fielden.platform.entity.AbstractEntity;

import java.util.Optional;

/**
 * A contract to generate entity master uri for specific entity.
 */
public interface IUriGenerator {

    /**
     * Generates entity master URI for specified entity. It might return empty URI string if entity doesn't have registered entity master.
     *
     * @param entity
     * @return
     * @param <T>
     */
    <T extends AbstractEntity<?>> Optional<String> generateUri(T entity);
}
