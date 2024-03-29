package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract to specify y-axis title of scatter plot.
 *
 * @param <T>
 */
public interface IScatterPlotYAxisTitle<T extends AbstractEntity<?>> extends IScatterPlotTopMargin<T> {

    /**
     * Sets the title of y-axis
     *
     * @param title
     * @return
     */
    IScatterPlotTopMargin<T> withYAxisTitle(String title);
}
