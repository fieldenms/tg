package ua.com.fielden.platform.swing.categorychart;

import java.util.List;

import org.jfree.chart.JFreeChart;

import ua.com.fielden.platform.equery.lifecycle.IProgressUpdater;

/**
 * Contract for chart creation factory.
 * 
 * TODO : this interface should be re-factored to be more "multiple-charts" centric, to work with {@link MultipleChartPanel} rather than {@link ActionChartPanel}.
 * 
 * @author oleh
 * 
 */
public interface IChartFactory<M, T> {

    /**
     * Returns {@link JFreeChart} instances for specified type. The type of chart is specified with {@link CategoryChartTypes} parameter.
     * 
     * @param type
     *            - specified type of chart.
     * @return
     */
    List<JFreeChart> getCharts(T type);

    /**
     * Sets the model to be used for charts creation. Second parameter determines whether to show all series of the chart or not. If second parameter is set to false then at least
     * one index must be specified otherwise the chart will be empty.
     * 
     * @param model
     */
    void setModel(final M model, boolean all, int... indexes);

    /**
     * Sets the model to be used for charts creation. Second parameter determines whether to show all series of the chart or not. If second parameter is set to false then at least
     * one index must be specified otherwise the chart will be empty.
     * 
     * @param model
     */
    M getModel();

    /**
     * Initiates datasets based on provided <code>model</code> and <code>chartType</code>. It could be performed on separate thread if it is heavy-weight operation.
     * 
     * @param type
     */
    void initDatasets(final T type, final IProgressUpdater progressUpdater);

}
