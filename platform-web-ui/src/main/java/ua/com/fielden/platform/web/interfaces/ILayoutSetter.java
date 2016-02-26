package ua.com.fielden.platform.web.interfaces;

/**
 * Helper interface, it just hides the device sensitive layout API. Introduced in order to provide chaining API.
 *
 * @author TG Team
 *
 */
public interface ILayoutSetter<T extends ILayout<?>> {

    /**
     * Set the layout spec.
     *
     * @param layout
     * @return
     */
    T set(final String layout);
}
