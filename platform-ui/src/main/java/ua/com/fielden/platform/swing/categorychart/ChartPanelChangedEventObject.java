package ua.com.fielden.platform.swing.categorychart;

import java.util.EventObject;

import org.jfree.chart.JFreeChart;

/**
 * Event object that encapsulates information about changes to the {@link ActionChartPanel}.
 * 
 * @author oleh
 * 
 */
public class ChartPanelChangedEventObject extends EventObject {

    private static final long serialVersionUID = 3020928347330964331L;

    private final JFreeChart newChart;

    /**
     * Initiates the {@link ChartPanelChangedEventObject} with sourceObject and newChart parameters.
     * 
     * @param source
     * @param newChart
     *            - new chart that was set to the chart panel.
     */
    public ChartPanelChangedEventObject(final Object source, final JFreeChart newChart) {
        super(source);
        this.newChart = newChart;
    }

    /**
     * Returns newChart property of this {@link ChartPanelChangedEventObject}. See {@link #ChartPanelChangedEventObject(Object, JFreeChart)} for more information about the newChart
     * property.
     * 
     * @return
     */
    public JFreeChart getNewChart() {
        return newChart;
    }

}
