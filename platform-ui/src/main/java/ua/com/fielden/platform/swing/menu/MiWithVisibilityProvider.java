package ua.com.fielden.platform.swing.menu;

import ua.com.fielden.platform.swing.view.BaseNotifPanel;

public class MiWithVisibilityProvider<V extends BaseNotifPanel> extends TreeMenuItem<V> {

    private final ITreeMenuItemVisibilityProvider visibilityProvider;

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
