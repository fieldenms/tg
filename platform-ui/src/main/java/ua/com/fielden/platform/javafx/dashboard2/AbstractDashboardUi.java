package ua.com.fielden.platform.javafx.dashboard2;

import java.util.List;

import javax.swing.JComponent;

import ua.com.fielden.platform.swing.menu.StubUiModel;
import ua.com.fielden.platform.swing.view.BaseNotifPanel;

/**
 * UI class for dashboard.
 *
 * @author TG Team
 *
 */
public abstract class AbstractDashboardUi<DASHBOARD_MODEL extends AbstractDashboard<? extends AbstractDashboardUi>> extends BaseNotifPanel<StubUiModel> {
    private static final long serialVersionUID = 7623224149991897675L;
    private final JComponent paramsGetterUi;
    private final List<IDashboardItemUi> dashboardItemUis;
    private final DASHBOARD_MODEL dashboardModel;

    public AbstractDashboardUi(final JComponent paramsGetterUi, final List<IDashboardItemUi> dashboardItemUis, final DASHBOARD_MODEL dashboardModel) {
	super("Dashboard", new StubUiModel(true));

	this.dashboardModel = dashboardModel;
	this.paramsGetterUi = paramsGetterUi;
	this.dashboardItemUis = dashboardItemUis;
    }

    @Override
    public abstract String getInfo();

    @Override
    protected abstract void layoutComponents();

    protected JComponent getParamsGetterUi() {
	return paramsGetterUi;
    }

    protected List<IDashboardItemUi> getDashboardItemUis() {
	return dashboardItemUis;
    }

    public DASHBOARD_MODEL getDashboardModel() {
	return dashboardModel;
    }
}