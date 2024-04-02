package ua.com.fielden.platform.web.view.master.scatterplot.api;

/**
 * A contract that returns the string representation the source of data range and property name
 *
 * @author TG Team
 */
public interface IScatterPlotRangeConfig {

    /**
     *  Returns the string representation of data source and property name to calculate the range of scatter plot axis.
     * @return
     */
    String getSource();
}
