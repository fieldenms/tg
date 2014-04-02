package ua.com.fielden.platform.client.ui.menu;

import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.swing.menu.ITreeMenuItemVisibilityProvider;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemInvisibilityController;

public class TreeMenuItemVisibilityProvider implements ITreeMenuItemVisibilityProvider {

    private final MainMenuItem menuItem;
    private final User owner;
    private final IMainMenuItemInvisibilityController mmiController;

    public TreeMenuItemVisibilityProvider(final MainMenuItem menuItem, final User owner, final IMainMenuItemInvisibilityController mmiController) {
        this.menuItem = menuItem;
        this.owner = owner;
        this.mmiController = mmiController;
    }

    @Override
    public void setVisible(final boolean visible) {
        if (owner.isBase()) {
            if (visible) {
                mmiController.makeVisible(menuItem, owner);
            } else {
                mmiController.makeInvisible(menuItem, owner);
            }
        }
    }

    @Override
    public boolean isVisible() {
        return menuItem.isVisible();
    }

}
