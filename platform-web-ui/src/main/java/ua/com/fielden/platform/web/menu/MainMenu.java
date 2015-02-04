package ua.com.fielden.platform.web.menu;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.web.interfaces.ILayout;
import ua.com.fielden.platform.web.interfaces.IRenderable;

/**
 * Represents the main application menu that is accessable via the FAB button of the each custom web view, except those which are displayed as modal dialogs.
 *
 * @author TG Team
 *
 */
public class MainMenu implements IRenderable {

    private ILayout layout;

    private final List<MainMenuItem> items = new ArrayList<>();

    /**
     * Specifies the layout for the main menu.
     *
     * @param layout
     * @return
     */
    public MainMenu setLayout(final ILayout layout) {
        this.layout = layout;
        return this;
    }

    public MainMenu addMenuItem(final MainMenuItem menuItem) {
        this.items.add(menuItem);
        return this;
    }

    @Override
    public DomElement render() {
        return null;
    }
}
