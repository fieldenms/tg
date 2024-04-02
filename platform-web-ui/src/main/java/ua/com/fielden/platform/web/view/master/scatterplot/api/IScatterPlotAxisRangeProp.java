package ua.com.fielden.platform.web.view.master.scatterplot.api;

/**
 * A contract to specify property name of data source for axis range.
 *
 * @author TG Team
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
