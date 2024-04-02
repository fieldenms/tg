package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract to specify x-axis title of scatter plot.
 *
 * @param <T>
 *
 * @author TG Team
 */
public interface IScatterPlotXAxisTitle<T extends AbstractEntity<?>> extends IScatterPlotYAxisTitle<T> {

    /**
     * Sets the title of x-axis
     *
     * @param title
     * @return
     */
    IScatterPlotYAxisTitle<T> withXAxisTitle(String title);
}
