package ua.com.fielden.platform.web.menu;

/**
 * The contract for main menu item configuration object.
 *
 * @author TG Team
 *
 */
public interface IMainMenuItemConfig extends IMenuLayoutConfig<IMainMenuItemConfig> {

    /**
     * Set the title for the main menu item.
     *
     * @param title
     * @return
     */
    IMainMenuItemConfig title(String title);

    /**
     * Set the resource path to the main menu item icon.
     *
     * @param icon
     * @return
     */
    IMainMenuItemConfig icon(String icon);

    /**
     * Set the resource path to the main menu item background image.
     *
     * @param backgroundImage
     * @return
     */
    IMainMenuItemConfig backgroundImage(String backgroundImage);

    /**
     * Complete the main menu item configuration.
     *
     * @return
     */
    IMainMenuConfig done();
}
