package ua.com.fielden.platform.web.menu;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.layout.FlexLayout;

public class MainMenuItem implements IRenderable {

    /**
     * Menu item title.
     */
    private final String title;
    /**
     * Path to the main menu item icon.
     */
    private final String icon;
    /**
     * Path to the background image of the main menu item.
     */
    private final String backgroundImage;

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
        final FlexLayout layout = new FlexLayout();
        return null;
    }

}
