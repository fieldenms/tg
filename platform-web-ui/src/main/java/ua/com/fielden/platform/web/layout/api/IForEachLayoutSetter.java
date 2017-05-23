package ua.com.fielden.platform.web.layout.api;

import ua.com.fielden.platform.web.layout.api.impl.ContainerCellLayoutConfig;
import ua.com.fielden.platform.web.layout.api.impl.FlexLayoutConfig;

/**
 * A contract for specifying layout configuration for each element in the container that has no specified layout configuration directly.
 *
 * @author TG Team
 *
 */
public interface IForEachLayoutSetter extends IGap {

    /**
     * Set the layout for each element in the container with missing layout configuration.
     *
     * @param layout
     * @return
     */
    ContainerCellLayoutConfig layoutForEach(final FlexLayoutConfig layout);
}
