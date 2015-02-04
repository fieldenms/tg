package ua.com.fielden.platform.web.menu;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.web.interfaces.IRenderable;

public class MainMenuItem implements IRenderable {

    /**
     * Menu item title.
     */
    public final String title;
    /**
     * Path to the main menu item icon.
     */
    public final String icon;
    /**
     * Path to the background image of the main menu item.
     */
    public final String backgroundImage;

    /**
     * Creates new main menu item with a title and an icon.
     *
     * @param title
     * @param icon
     */
    public MainMenuItem(final String title, final String icon, final String backgroundImage) {
        this.title = title;
        this.icon = icon;
        this.backgroundImage = backgroundImage;
    }

    @Override
    public DomElement render() {
        return new DomElement("tg-main-menu-item").
                attr("whenDesktop", "[background-image: url(" + backgroundImage + "), center, end-justified [], []]").
                attr("whenTablet", "[background-image: url(" + backgroundImage + "), [], []]").
                attr("whenMobile", "[background-image: url(" + backgroundImage + "), [], []]").
                attr("menuIcon", icon).
                attr("menuItemTitle", title);
    }
}
