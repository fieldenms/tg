package ua.com.fielden.platform.swing.categorychart;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.event.EventListenerList;

/**
 * Responsible for switching between different chart types.
 *
 * @author oleh
 *
 */
public class SwitchChartsModel<M, T> {

    private final EventListenerList listeners = new EventListenerList();

    private final MultipleChartPanel<M, T> chartPanel;

    private final IChartUpdateListener chartUpdate;

    private T currentType;

    public SwitchChartsModel(final MultipleChartPanel<M, T> chartPanel) {
	this.chartPanel = chartPanel;
	this.chartUpdate = createChartUpdateListener();
    }

    private IChartUpdateListener createChartUpdateListener() {
	return new IChartUpdateListener() {

	    private int chartUpdated = 0;

	    @Override
	    public void chartWasUpdated(final ChartUpdateEvent event) {
		chartUpdated++;
		event.getSource().removeChartUpdateListener(this);
		if(chartUpdated == chartPanel.getChartPanelCount()){
		    fireCurrentChartTypeChangedListener(new ChartTypeChangedEvent(SwitchChartsModel.this));
		    chartUpdated = 0;
		}
	    }
	};
    }

    public T getCurrentType() {
	return currentType;
    }

    protected void setCurrentType(final T currentType) {
	this.currentType = currentType;
    }

    public ItemListener createListenerForChartType(final T type) {
	return new ChartTypeChangeListener(type);
    }

    public void addCurrentChartTypeChagedListener(final ICurrentChartTypeChangedListener listener){
	listeners.add(ICurrentChartTypeChangedListener.class, listener);
    }

    public void removeCurrentChartTypeChangedListener(final ICurrentChartTypeChangedListener listener){
	listeners.remove(ICurrentChartTypeChangedListener.class, listener);
    }

    private void fireCurrentChartTypeChangedListener(final ChartTypeChangedEvent event) {
	for (final ICurrentChartTypeChangedListener listener : listeners.getListeners(ICurrentChartTypeChangedListener.class)) {
	    listener.chartTypeChanged(event);
	}
    }

    /**
     * Listener for chart type change events.
     *
     * @author Jhou
     *
     */
    public class ChartTypeChangeListener implements ItemListener {
	private final T type;

	public ChartTypeChangeListener(final T type) {
	    this.type = type;
	}

	@Override
	public void itemStateChanged(final ItemEvent e) {
	    if (e.getStateChange() == ItemEvent.SELECTED) {
		currentType = type;
		if (chartPanel.getChartPanelCount() > 0) {
		    chartPanel.getChartPanel(0).setPostAction(new Runnable() {

			@Override
			public void run() {
			    for (int index = 1; index < chartPanel.getChartPanelCount(); index++) {
				final ActionChartPanel<M, T> panel = chartPanel.getChartPanel(index);
				panel.setPostAction(null);
				panel.addChartUpdateListener(chartUpdate);
				panel.setChart(type);
			    }
			}

		    });
		    chartPanel.getChartPanel(0).addChartUpdateListener(chartUpdate);
		    chartPanel.getChartPanel(0).setChart(type);
		}
	    }
	}
    }
}
