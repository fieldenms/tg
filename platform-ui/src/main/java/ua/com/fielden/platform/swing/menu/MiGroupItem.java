package ua.com.fielden.platform.swing.menu;

/**
 * The menu item representing a grouping node in the menu tree. It does not have any view, just the title and some info about it.
 * 
 * @author TG Team
 * 
 */
public class MiGroupItem extends MiWithVisibilityProvider {

    private static final long serialVersionUID = -589961357292227245L;

    /**
     * Principle constructor.
     * 
     * @param visibilityProvider
     *            -- determines visibility of the item (originally intended to be based on the logged user information).
     * @param title
     *            -- title of the item displayed in the tree menu.
     * @param info
     *            -- information displayed in the information panel of the menu item.
     */
    public MiGroupItem(final ITreeMenuItemVisibilityProvider visibilityProvider, final String title, final String info) {
        super(visibilityProvider, title, info);
    }
}
