package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Contract for assigning left margin of scatter plot
 *
 * @param <T>
 *
 * @author TG Team
 */
public interface IScatterPlotLeftMargin<T extends AbstractEntity<?>> extends IScatterPlotBottomMargin<T> {

    /**
     * Assigns the left margin of scatter plot.
     *
     * @param margin
     * @return
     */
    IScatterPlotBottomMargin<T> leftMargin(int margin);
}
