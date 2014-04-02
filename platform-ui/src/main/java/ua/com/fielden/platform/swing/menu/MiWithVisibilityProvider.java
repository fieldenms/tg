package ua.com.fielden.platform.swing.menu;

import ua.com.fielden.platform.swing.view.BasePanel;

/**
 * {@link TreeMenuItem} that must be initiated with specified {@link ITreeMenuItemVisibilityProvider} instance.
 * 
 * @author TG Team
 * 
 * @param <V>
 */
public class MiWithVisibilityProvider<V extends BasePanel> extends TreeMenuItem<V> {

    private static final long serialVersionUID = -7646023613806982744L;

    private final ITreeMenuItemVisibilityProvider visibilityProvider;

    /**
     * Principle constructor. This constructor should be used for group menu items.
     * 
     * @param visibilityProvider
     *            -- determines visibility of the item (originally intended to be based on the logged user information).
     * @param title
     *            -- title of the item displayed in the tree menu.
     * @param info
     *            -- information displayed in the information panel of the menu item.
     */
    public MiWithVisibilityProvider(final ITreeMenuItemVisibilityProvider visibilityProvider, final String title, final String info) {
        super(title, info);
        this.visibilityProvider = visibilityProvider;
        super.setVisible(visibilityProvider != null ? visibilityProvider.isVisible() : true);
    }

    /**
     * This is a convenience constructor that initialises this menu item with specified view and {@link ITreeMenuItemVisibilityProvider} instance. The info panel is not required.
     * 
     * @param view
     * @param visibilityProvider
     *            -- determines visibility of the item (originally intended to be based on the logged user information).
     */
    public MiWithVisibilityProvider(final V view, final ITreeMenuItemVisibilityProvider visibilityProvider) {
        super(view);
        this.visibilityProvider = visibilityProvider;
        super.setVisible(visibilityProvider != null ? visibilityProvider.isVisible() : true);
    }

    @Override
    public void setVisible(final boolean visible) {
        if (isVisible() == visible) {
            return;
        }
        super.setVisible(visible);
        if (visibilityProvider != null) {
            visibilityProvider.setVisible(visible);
        }
    }

}
