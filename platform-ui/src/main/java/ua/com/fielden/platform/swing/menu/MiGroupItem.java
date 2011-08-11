package ua.com.fielden.platform.swing.menu;

public class MiGroupItem extends TreeMenuItem {

    private final ITreeMenuItemVisibilityProvider visibilityProvider;

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
