package ua.com.fielden.platform.swing.menu;

/**
 * A contract for setting visibility of of the main menu items.
 * 
 * @author TG Team
 * 
 */
public interface ITreeMenuItemVisibilityProvider {

    void setVisible(boolean visible);

    boolean isVisible();

}
