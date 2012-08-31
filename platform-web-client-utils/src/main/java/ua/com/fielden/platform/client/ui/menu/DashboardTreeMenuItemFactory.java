package ua.com.fielden.platform.client.ui.menu;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.javafx.dashboard.DashboardNotifPanel;
import ua.com.fielden.platform.swing.menu.ITreeMenuItemVisibilityProvider;
import ua.com.fielden.platform.swing.menu.MiDashboard;
import ua.com.fielden.platform.swing.menu.MiWithVisibilityProvider;
import ua.com.fielden.platform.swing.menu.TreeMenuWithTabs;
import ua.com.fielden.platform.swing.menu.api.ITreeMenuItemFactory;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;

import com.google.inject.Injector;

public class DashboardTreeMenuItemFactory implements ITreeMenuItemFactory {

    @Override
    public MiWithVisibilityProvider<?> create(final Class<?> clazz, final TreeMenuWithTabs<?> treeMenu, final Injector injector, final ITreeMenuItemVisibilityProvider visibilityProvider) {
	final DashboardNotifPanel dashPanel = new DashboardNotifPanel(treeMenu, injector.getInstance(IGlobalDomainTreeManager.class), injector.getInstance(ICriteriaGenerator.class), injector.getInstance(IEntityMasterManager.class));
	final MiDashboard miDash = new MiDashboard(dashPanel, visibilityProvider);
	dashPanel.getDashboardView().setMiMyProfile(miDash);
	return miDash;
    }

}
