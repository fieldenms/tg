package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Contract for assigning top margin of scatter plot
 *
 * @param <T>
 *
 * @author TG Team
 */
public interface IScatterPlotTopMargin<T extends AbstractEntity<?>> extends IScatterPlotLeftMargin<T>{

    /**
     * Assigns the top margin of scatter plot.
     *
     * @param margin
     * @return
     */
    IScatterPlotLeftMargin<T> topMargin(int margin);
}
