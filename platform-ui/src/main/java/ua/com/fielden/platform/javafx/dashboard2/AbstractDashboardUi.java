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
public abstract class AbstractDashboardUi extends BaseNotifPanel<StubUiModel> {
    private static final long serialVersionUID = 7623224149991897675L;
    private final JComponent paramsGetterUi;
    private final List<IDashboardItemUi> dashboardItemUis;

    public AbstractDashboardUi(final JComponent paramsGetterUi, final List<IDashboardItemUi> dashboardItemUis) {
	super("Dashboard", new StubUiModel(true));

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
}