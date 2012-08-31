package ua.com.fielden.platform.javafx.dashboard;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.swing.menu.StubUiModel;
import ua.com.fielden.platform.swing.menu.TreeMenuWithTabs;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.swing.view.BaseNotifPanel;

public class DashboardNotifPanel extends BaseNotifPanel<StubUiModel> {
    private static final long serialVersionUID = 7623224149991897675L;

    private final DashboardView dashboardView;

    public DashboardNotifPanel(final TreeMenuWithTabs<?> treeMenu, //
	    final IGlobalDomainTreeManager globalManager, //
	    final ICriteriaGenerator criteriaGenerator, //
	    final IEntityMasterManager masterManager) {
	super("Dashbord report", new StubUiModel(true));

	this.dashboardView = new DashboardView(globalManager, criteriaGenerator, masterManager);
	add(dashboardView);

	dashboardView.setTreeMenu(treeMenu);
    }

    @Override
    public String getInfo() {
	return "Dashboard notif panel";
    }

    public DashboardView getDashboardView() {
	return dashboardView;
    }
}
