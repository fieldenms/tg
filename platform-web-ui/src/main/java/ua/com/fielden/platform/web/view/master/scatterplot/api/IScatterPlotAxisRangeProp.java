package ua.com.fielden.platform.web.view.master.scatterplot.api;

/**
 * A contract to specify property name for source of axis range.
 */
public interface IScatterPlotAxisRangeProp {

    /**
     * Set the property name that references the data that is used for calculating the range of scatter plot axis.
     *
     * @param propertyName
     * @return
     */
    IScatterPlotRangeConfig prop(String propertyName);
}
