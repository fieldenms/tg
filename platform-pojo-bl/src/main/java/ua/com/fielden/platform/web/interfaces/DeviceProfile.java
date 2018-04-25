package ua.com.fielden.platform.web.interfaces;

/**
 * Enumeration of supported web application profiles.
 * <p>
 * Currently, there are have two profiles:
 * <ul>
 * <li>DESKTOP
 * <li>MOBILE
 * </ul>
 * Each profile can be provided with its one main menu and other settings.
 *
 * @author TG Team
 *
 */
public enum DeviceProfile {
    /**
     * Desktop device profile for web application.
     */
    DESKTOP,
    /**
     * Mobile device profile for web application.
     */
    MOBILE
}
