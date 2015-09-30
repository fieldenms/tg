package ua.com.fielden.platform.web.view.master.api;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.entity.AbstractEntity;

/**
 *
 * A contract representing a final (complete) configuration of a Simple Master.
 *
 * @author TG Team
 *
 */
public interface ISimpleMasterConfig<T extends AbstractEntity<?>> extends IMaster<T> {

    /**
     * Returns custom matcher type for the specified property.
     *
     * @return
     */
    Class<? extends IValueMatcherWithContext<T, ?>> matcherTypeFor(final String propName);
}
