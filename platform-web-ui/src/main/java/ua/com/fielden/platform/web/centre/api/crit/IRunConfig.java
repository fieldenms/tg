package ua.com.fielden.platform.web.centre.api.crit;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.crit.layout.ILayoutConfig;

/**
 * A contract to force centre run automatically after load.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IRunConfig<T extends AbstractEntity<?>> extends ILayoutConfig<T> {
    /**
     * Forces centre to run automatically after it was loaded.
     *
     * @return
     */
    ILayoutConfig<T> runAutomatically();
}
