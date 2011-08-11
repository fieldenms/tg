package ua.com.fielden.platform.swing.categorychart;

import java.util.EventListener;

/**
 * Interface that must be supported by classes those wish to receive notification when the chart of the {@link ActionChartPanel} changed.
 * 
 * @author oleh
 * 
 */
public interface IChartPanelChangeListener extends EventListener {

    /**
     * Receives notification of a chart panel changed event.
     * 
     * @param event
     */
    void chartPanelChanged(final ChartPanelChangedEventObject event);
}
