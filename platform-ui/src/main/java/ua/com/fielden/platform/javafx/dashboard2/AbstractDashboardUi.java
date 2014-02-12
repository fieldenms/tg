package ua.com.fielden.platform.javafx.dashboard2;

import java.util.List;

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
    private final List<IDashboardItemUi> dashboardItemUis;

    public AbstractDashboardUi(final List<IDashboardItemUi> dashboardItemUis) {
	super("Dashboard", new StubUiModel(true));

	this.dashboardItemUis = dashboardItemUis;
    }

    @Override
    public abstract String getInfo();

//    @Override
//    public String getInfo() {
//	return "<html>" + "<h3>Dashboard</h3>"//
//		+ "A facility for managing and running user-centric sentinels." //
//		+ "</html>";
//    }

//    @Override
//    protected void layoutComponents() {
//	setLayout(new MigLayout("insets 0", "[:50:]20[:50:]20[:50:]", "[c][c]"));
//	add(getNotifPanel(), "wrap");
//
//	for (int i = 0; i < dashboardItemUis.size(); i++) {
//	    addComponent(getHoldingPanel(), dashboardItemUis.get(i).upperComponent(), i % 3 == 0 ? "wrap" : ""); // TODO
//	}
//	// example: new MigLayout("insets 0", "[:50:][grow,fill,:80:]20[:50:][grow,fill,:80:]20[:50:][grow,fill,:200:]", "[c]");
//    }

    @Override
    protected abstract void layoutComponents();

    protected List<IDashboardItemUi> getDashboardItemUis() {
	return dashboardItemUis;
    }
}