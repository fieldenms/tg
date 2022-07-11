package ua.com.fielden.platform.menu;

import java.util.List;
import java.util.Optional;

/**
 * Contract for main application menu needed to adjust visibility or semi-visibility for menu items
 *
 * @author TG Team
 *
 */
public interface IMenuManager {

    /**
     * Returns optional menu item with title.
     *
     * @param title
     * @return
     */
    <U extends IMenuManager> Optional<U> getMenuItem(String title);

    /**
     * Remove menu item that has specified title.
     *
     * @param title
     * @return
     */
    boolean removeMenuItem(String title);

    /**
     * Makes invisible menu item with specified title.
     *
     * @param title
     */
    void makeMenuItemInvisible(String title);

    /**
     * Makes semi visible menu item with specified title
     *
     * @param title
     */
    void makeMenuItemSemiVisible(String title);

    /**
     * Tests this menu item visibility.
     *
     * @return
     */
    public boolean isVisible();

    /**
     * Tests semi visibility for this menu item.
     *
     * @return
     */
    public boolean isSemiVisible();

    /**
     * Returns sub menu for this menu item.
     *
     * @return
     */
    <U extends IMenuManager> List<U> getMenu();

    /**
     * Returns title for this menu item.
     *
     * @return
     */
    String getTitle();
}
