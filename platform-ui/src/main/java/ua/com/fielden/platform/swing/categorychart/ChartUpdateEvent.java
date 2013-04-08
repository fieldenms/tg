package ua.com.fielden.platform.swing.categorychart;

import java.util.EventObject;

public class ChartUpdateEvent extends EventObject {

    private static final long serialVersionUID = 1277985101636556330L;

    public enum ChartUpdateAction{
	CHART_UPDATED_SUCCESSFUL, CHART_UPDATE_FAILED;
    }

    private final ChartUpdateAction chartAction;

    public ChartUpdateEvent(final ActionChartPanel<?, ?> source, final ChartUpdateAction chartAction) {
	super(source);
	this.chartAction = chartAction;
    }

    @Override
    public ActionChartPanel<?, ?> getSource() {
        return (ActionChartPanel<?, ?>)super.getSource();
    }

    public ChartUpdateAction getChartAction() {
	return chartAction;
    }
}
