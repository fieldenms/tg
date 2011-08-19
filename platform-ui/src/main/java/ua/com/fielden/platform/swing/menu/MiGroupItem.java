package ua.com.fielden.platform.swing.menu;

/**
 * The menu item representing a grouping node in the menu tree. It does not have any view, just the title and some info about it.
 *
 * @author TG Team
 *
 */
public class MiGroupItem extends TreeMenuItem {

    private final ITreeMenuItemVisibilityProvider visibilityProvider;

    /**
     * Principle constructor.
     *
     * @param visibilityProvider -- determines visibility of the item (originally intended to be based on the logged user information).
     * @param title -- title of the item displayed in the tree menu.
     * @param info -- information displayed in the information panel of the menu item.
     */
    public MiGroupItem(final ITreeMenuItemVisibilityProvider visibilityProvider, final String title, final String info) {
	super(title, info);
	this.visibilityProvider = visibilityProvider;
	super.setVisible(visibilityProvider != null ? visibilityProvider.isVisible() : true);
    }

    @Override
    public void setVisible(final boolean visible) {
	if (isVisible() == visible) {
	    return;
	}
	if (visibilityProvider != null) {
	    visibilityProvider.setVisible(visible);
	}
	super.setVisible(visible);

    }
}
