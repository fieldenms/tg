package ua.com.fielden.platform.web.interfaces;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.web.component.AbstractWebComponent;

/**
 * The web layout manager contract.
 *
 * @author TG Team
 *
 */
public interface ILayout extends IRenderable {

    /**
     * Represents the list of supported devices.
     *
     * @author TG Team
     *
     *         - a component to be added to layout manager.
     */
    public enum Device {
        DESKTOP,
        TABLET,
        MOBILE,
        PRINT;
        @Override
        public String toString() {
            return StringUtils.capitalize(name().toLowerCase());
        };
    }

    /**
     * Represents the device orientation.
     *
     * @author TG Team
     *
     */
    public enum Orientation {
        LANDSCAPE,
        PORTRAIT;
        @Override
        public String toString() {
            return name().toLowerCase();
        };
    }

    /**
     * Adds the {@link AbstractWebComponent} to the layout manager.
     *
     * @param component
     *            - a component to be added to layout manager.
     * @param constraints
     *            - additional layout configuration parameters for the component.
     * @return
     */
    ILayout add(AbstractWebComponent component);

}
