package ua.com.fielden.platform.web.menu;

import java.awt.Color;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.web.interfaces.ILayout;
import ua.com.fielden.platform.web.interfaces.IRenderable;

public class MainMenuItem implements IRenderable {

    /**
     * Menu item title.
     */
    private String title;
    /**
     * Menu item description.
     */
    private String description;
    /**
     * Path to the main menu item icon.
     */
    private String icon;
    /**
     * Specifies the menu item background colour.
     */
    //Converting color to hex value String.format("#%02x%02x%02x", r, g, b);
    private Color backgroundColor;
    /**
     * Path to the background image of the main menu item.
     */
    private String backgroundImage;
    /**
     * Menu item layout for different devices. This layout should contain cells for icon, title and description
     */
    private ILayout layout;

    public MainMenuItem(final String title, final String icon) {

    }

    @Override
    public DomElement render() {
	// TODO Auto-generated method stub
	return null;
    }

}
