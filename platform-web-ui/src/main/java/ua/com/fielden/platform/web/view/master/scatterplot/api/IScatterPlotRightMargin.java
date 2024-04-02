package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Contract for assigning right margin of scatter plot
 *
 * @param <T>
 *
 * @author TG Team
 */
public interface IScatterPlotRightMargin<T extends AbstractEntity<?>> extends IScatterPlotTooltip<T> {

    /**
     * Assigns the right margin of scatter plot.
     *
     * @param margin
     * @return
     */
    IScatterPlotTooltip<T> rightMargin(int margin);
}
