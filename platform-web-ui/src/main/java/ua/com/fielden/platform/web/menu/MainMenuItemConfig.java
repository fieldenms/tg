package ua.com.fielden.platform.web.menu;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.ILayout.Orientation;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.layout.FlexLayout;

/**
 * An implementation of the {@link IMainMenuItemConfig}.
 *
 * @author TG Team
 *
 */
public class MainMenuItemConfig implements IMainMenuItemConfig, IRenderable {

    /**
     * Menu item title.
     */
    private final String title;
    /**
     * The description for this main menu item.
     */
    private String longDesc;
    /**
     * Path to the main menu item icon.
     */
    private String icon;
    /**
     * Path to the background image of the main menu item.
     */
    private String backgroundImage;
    /**
     * The main menu configuration object to which this menu item belongs to.
     */
    private final IMainMenuConfig mainMenuConfig;
    /**
     * The layout for this menu item.
     */
    private final FlexLayout flexLayout = new FlexLayout();

    /**
     * Creates new main menu item configuration object with main menu tow which this menu item belongs to.
     *
     */
    public MainMenuItemConfig(final IMainMenuConfig mainMenuConfig, final String title) {
        this.mainMenuConfig = mainMenuConfig;
        this.title = title;
    }

    @Override
    public IMainMenuItemConfig longDesc(final String longDesc) {
        this.longDesc = longDesc;
        return this;
    }

    @Override
    public IMainMenuItemConfig icon(final String icon) {
        this.icon = icon;
        return this;
    }

    @Override
    public IMainMenuItemConfig backgroundImage(final String backgroundImage) {
        this.backgroundImage = backgroundImage;
        return this;
    }

    @Override
    public IMainMenuConfig done() {
        return mainMenuConfig;
    }

    @Override
    public IMainMenuItemConfig setLayoutFor(final Device device, final Orientation orientation, final String layout) {
        this.flexLayout.whenMedia(device, null).set(layout);
        return this;
    }

    @Override
    public DomElement render() {
        return new DomElement("paper-shadow").clazz("category").
                attr("z", "1").
                add(flexLayout.render().style("background-image: url(" + backgroundImage + ")", "background-position: 50%").
                        add(new DomElement("core-icon").attr("src", icon)).
                        add(new DomElement("pre").
                                add(new InnerTextElement(title))));
    }
}
