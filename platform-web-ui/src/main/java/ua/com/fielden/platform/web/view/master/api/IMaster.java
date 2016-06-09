package ua.com.fielden.platform.web.view.master.api;

import java.util.Optional;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
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
     * Entity masters may or may not provide information about specific entity value matchers.
     *
     * @param propName
     * @return
     */
    Optional<Class<? extends IValueMatcherWithContext<T, ?>>> matcherTypeFor(final String propName);

    /**
     * Should be implemented by concrete entity master, returning an instance of IRenderable that is capable of rendering a completer master view.
     *
     * @return
     */
    IRenderable render();

}
