package ua.com.fielden.platform.swing.categorychart;

import java.util.EventObject;

public class MultipleChartPanelEvent extends EventObject {

    private static final long serialVersionUID = -2760536832144520875L;

    private final ActionChartPanel<?, ?> chartPanel;

    private final boolean removed;

    public MultipleChartPanelEvent(final Object source, final ActionChartPanel<?, ?> chartPanel, final boolean removed) {
	super(source);
	this.chartPanel = chartPanel;
	this.removed = removed;
    }

    public ActionChartPanel<?, ?> getChartPanel() {
	return chartPanel;
    }

    public boolean isRemoved() {
	return removed;
    }

}
