package ua.com.fielden.platform.web.layout.api;

import ua.com.fielden.platform.web.layout.api.impl.FlexLayoutConfig;

/**
 * Indicates the end of layout configuration.
 *
 * @author TG Team
 *
 */
public interface ILayoutCellCompleted {

    /**
     * Finish the layout configuration.
     *
     * @return
     */
    FlexLayoutConfig end();
}
