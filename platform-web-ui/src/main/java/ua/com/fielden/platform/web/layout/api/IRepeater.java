package ua.com.fielden.platform.web.layout.api;

import ua.com.fielden.platform.web.layout.api.impl.ContainerRepeatConfig;

/**
 * A contract for copying the layout configuration.
 *
 * @author TG Team
 *
 */
public interface IRepeater extends IForEachLayoutSetter {

    /**
     * Repeats the previous container layout configuration the specified number of items.
     *
     * @param times
     * @return
     */
    ContainerRepeatConfig repeat(int times);

}
