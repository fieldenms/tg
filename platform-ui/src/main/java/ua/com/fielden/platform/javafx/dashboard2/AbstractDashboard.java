package ua.com.fielden.platform.javafx.dashboard2;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

/**
 * A dashboard facility contains some number of items (e.g. sentinels, calendar, charts etc.) that
 * should be displayed as an actual informational items. These items should be easily refreshable
 * and should alert the user in case of some bad situation.
 *
 * @author TG Team
 *
 */
public abstract class AbstractDashboard<UI extends AbstractDashboardUi> {
    private final List<IDashboardItem<?, ?>> items;
    private final IDashboardParamsGetter paramsGetter;
    private final UI dashboardUi;

    public AbstractDashboard(final IDashboardParamsGetter paramsGetter, final IDashboardItem<?, ?> ... dashboardItems) {
	this.paramsGetter = paramsGetter;
	items = new ArrayList<IDashboardItem<?, ?>>();
	for (final IDashboardItem<?, ?> dashboardItem : dashboardItems) {
	    items.add(dashboardItem);
	}

	dashboardUi = createDashboardUi(paramsGetter.getUI(), dashboardItems);
	// layout all items
	layoutAll();
    }

    protected abstract UI createDashboardUi(final JComponent paramsGetterUi, final IDashboardItem<?, ?>... dashboardItems);
//    {
//	return new AbstractDashboardUi(createDashboardItemUis(dashboardItems));
//    }

    protected List<IDashboardItemUi> createDashboardItemUis(final IDashboardItem<?, ?>[] dashboardItems) {
	final List<IDashboardItemUi> dashboardItemUis = new ArrayList<>();
	for (final IDashboardItem<?, ?> dashboardItem : dashboardItems) {
	    dashboardItemUis.add(dashboardItem.getUi());
	}
	return dashboardItemUis;
    }

    /**
     * Refreshes all items in asynchronous way.
     */
    public void refreshAll() {
	for (final IDashboardItem<?, ?> item : items) {
	    item.runAndDisplay(paramsGetter.getCustomParams());
	}
    }

    /**
     * Provide layout of all items.
     */
    public void layoutAll() {
	dashboardUi.layoutComponents();
    }

    public UI getUi() {
	return dashboardUi;
    }
}
