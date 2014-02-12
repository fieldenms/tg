package ua.com.fielden.platform.swing.menu;

import ua.com.fielden.platform.javafx.dashboard2.AbstractDashboardUi;
import ua.com.fielden.platform.utils.ResourceLoader;

public class MiDashboard2 extends MiWithVisibilityProvider<AbstractDashboardUi> {
    private static final long serialVersionUID = -6025618170129278581L;

    public MiDashboard2(final AbstractDashboardUi view, final ITreeMenuItemVisibilityProvider visibilityProvider) {
	super(view, visibilityProvider);
	setIcon(ResourceLoader.getIcon("images/asset_status.png")); // TODO provide its own icon
    }
}
