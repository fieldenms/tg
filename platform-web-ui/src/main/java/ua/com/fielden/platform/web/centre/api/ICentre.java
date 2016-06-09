package ua.com.fielden.platform.web.centre.api;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.interfaces.IRenderable;

/**
 *
 * An contract for entity centre UI. It should be implemented by classes that represent a specific entity centre.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface ICentre<T extends AbstractEntity<?>> {

    /**
     * Should be implemented by concrete entity centre, returning an instance of IRenderable that is capable of rendering a complete centre view.
     *
     * @return
     */
    IRenderable build();

}
