package ua.com.fielden.platform.web.view.master.api;

import java.util.Map;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.interfaces.IRenderable;

/**
 *
 * A contract representing a final (complete) configuration of a Simple Master.
 *
 * @author TG Team
 *
 */
public interface ISimpleMasterConfig<T extends AbstractEntity<?>> {

    /**
     * The most basic of methods
     * @return
     */
    IRenderable render();

    @SuppressWarnings("rawtypes")
    /**
     * Returns a map between properties and custom value matchers.
     *
     * @return
     */
    Map<String, Class<IValueMatcher>> propValueMatchers();
}
