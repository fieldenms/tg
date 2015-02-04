package ua.com.fielden.platform.web.master.api;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.interfaces.IRenderable;

/**
 *
 * An contract for entity master UI. It should be implemented by classes that represent a specific entity master.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IMaster<T extends AbstractEntity<?>> {

    /**
     * Should be implemented by concrete entity master, returning an instance of IRenderable that is capable of rendering a completer master view.
     *
     * @return
     */
    IRenderable build();

}
