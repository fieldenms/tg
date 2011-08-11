package ua.com.fielden.platform.swing.categorychart;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.EventListenerList;

import net.miginfocom.swing.MigLayout;

public class MultipleChartPanel<M, T> extends JScrollPane {

    private static final long serialVersionUID = 8064024233551027989L;

    private EventListenerList listenerList = new EventListenerList();

    private final JPanel viewport;

    public MultipleChartPanel() {
	super(new JPanel(new MigLayout("fill, insets 0,  wrap 1", "[fill]", "[fill]")), VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
	this.viewport = (JPanel) getViewport().getView();
    }

    public void addChartPanel(final ActionChartPanel<M, T> chartPanel) {
	viewport.add(chartPanel);
	fireMultipleChartPanelListener(new MultipleChartPanelEvent(this, chartPanel, false));
    }

    public ActionChartPanel<M, T> removeChartPanel(final int index) {
	final ActionChartPanel<M, T> chartPanel = getChartPanel(index);
	removeChartPanel(chartPanel);
	return chartPanel;
    }

    public void removeChartPanel(final ActionChartPanel<M, T> chartPanel) {
	viewport.remove(chartPanel);
	fireMultipleChartPanelListener(new MultipleChartPanelEvent(this, chartPanel, true));
    }

    @SuppressWarnings("unchecked")
    public ActionChartPanel<M, T> getChartPanel(final int index) {
	return (ActionChartPanel<M, T>) viewport.getComponent(index);
    }

    public void removeAllChartPanels() {
	for (int componentIndex = 0; componentIndex < viewport.getComponentCount(); componentIndex++) {
	    removeChartPanel(componentIndex);
	}
    }

    public void addAllChartPanels(final List<ActionChartPanel<M, T>> chartPanels) {
	for (final ActionChartPanel<M, T> chartPanel : chartPanels) {
	    addChartPanel(chartPanel);
	}
    }

    public List<ActionChartPanel<M, T>> getAllChartPanels() {
	final List<ActionChartPanel<M, T>> chartPanels = new ArrayList<ActionChartPanel<M, T>>();
	for (int componentIndex = 0; componentIndex < viewport.getComponentCount(); componentIndex++) {
	    chartPanels.add(getChartPanel(componentIndex));
	}
	return chartPanels;
    }

    public int getChartPanelsCount() {
	return viewport.getComponentCount();
    }

    public int indexOfChartPanel(final ActionChartPanel<M, T> chartPanel) {
	for (int chartPanelIndex = 0; chartPanelIndex < getChartPanelsCount(); chartPanelIndex++) {
	    if (getChartPanel(chartPanelIndex).equals(chartPanel)) {
		return chartPanelIndex;
	    }
	}
	return -1;
    }

    public void changeChartPosition(final int oldIndex, final int newIndex) {
	final List<ActionChartPanel<M, T>> allCharts = getAllChartPanels();
	final ActionChartPanel<M, T> chart = allCharts.remove(oldIndex);
	if (newIndex >= allCharts.size()) {
	    allCharts.add(chart);
	} else {
	    allCharts.add(newIndex, chart);
	}
	for (int componentIndex = 0; componentIndex < viewport.getComponentCount(); componentIndex++) {
	    viewport.remove(componentIndex);
	}
	for (int componentIndex = 0; componentIndex < allCharts.size(); componentIndex++) {
	    viewport.add(allCharts.get(componentIndex));
	}
    }

    public void addMultipleChartPanelListener(final MultipleChartPanelListener l) {
	listenerList.add(MultipleChartPanelListener.class, l);
    }

    public void removeMultipleChartPanelListener(final MultipleChartPanelListener l) {
	listenerList.remove(MultipleChartPanelListener.class, l);
    }

    protected void fireMultipleChartPanelListener(final MultipleChartPanelEvent event) {
	// Guaranteed to return a non-null array
	final Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length - 2; i >= 0; i -= 2) {
	    if (listeners[i] == MultipleChartPanelListener.class) {
		// Lazily create the event:
		((MultipleChartPanelListener) listeners[i + 1]).valueChanged(event);
	    }
	}
    }

}
