package ua.com.fielden.platform.client.config;

import ua.com.fielden.platform.swing.menu.api.ITreeMenuFactory;


/**
 * A contract, which should be implemented by each web client application to bind relevant main menu item factories their construction during the client startup.
 *
 * @author TG Team
 *
 */
public interface IMainMenuBinder {
    /**
     * Mutates(!) <code>menuFactory</code> parameter by assigning factories to relevant main menu items.
     *
     * @param menuFactory
     */
    void bindMainMenuItemFactories(final ITreeMenuFactory menuFactory);
}
