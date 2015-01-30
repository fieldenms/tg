package ua.com.fielden.platform.web.menu;

import java.awt.Color;

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
     * Menu item description.
     */
    private String description;
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
     * Creates new main menu item with a title and an icon.
     *
     * @param title
     * @param icon
     */
    public MainMenuItem(final String title, final String icon) {
	this.title = title;
	this.icon = icon;
    }

    /**
     * Set the description for this main menu item.
     *
     * @param description
     * @return
     */
    public MainMenuItem setDescription(final String description) {
	this.description = description;
	return this;
    }

    /**
     * Set the background image for this main menu item.
     *
     * @param backgroundImage
     * @return
     */
    public MainMenuItem setBackgroundImage(final String backgroundImage) {
	this.backgroundImage = backgroundImage;
	return this;
    }

    /**
     * Set the background colour for this main menu item.
     *
     * @param backgroundColor
     * @return
     */
    public MainMenuItem setBackgroundColor(final Color backgroundColor) {
	this.backgroundColor = backgroundColor;
	return this;
    }

    @Override
    public DomElement render() {
	final FlexLayout layout = new FlexLayout();
	return null;
    }

}
