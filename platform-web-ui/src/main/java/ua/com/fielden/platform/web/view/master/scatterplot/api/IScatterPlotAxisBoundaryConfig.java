package ua.com.fielden.platform.web.view.master.scatterplot.api;

/**
 * A contract to obtain a string representation for a data source and a property name within the data source to calculate the boundaries of values for a scatter plot axis (OY or OX).
 *
 * @author TG Team
 */
public interface IScatterPlotAxisBoundaryConfig {

    /**
     * Returns a string representation of a data source and a property name within the data source to calculate the boundaries of values for a scatter plot axis (OY or OX).
     * @return
     */
    String getSource();

}
