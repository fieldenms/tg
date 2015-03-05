package ua.com.fielden.platform.web.view.master.api;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
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

    /**
     * Returns custom matcher type for the specified property.
     *
     * @return
     */
    <V extends AbstractEntity<?>> Class<IValueMatcherWithContext<T, V>> matcherTypeFor(final String propName);
}
