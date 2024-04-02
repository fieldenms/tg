package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Contract for assigning bottom margin of scatter plot
 *
 * @param <T>
 *
 * @author TG Team
 */
public interface IScatterPlotBottomMargin<T extends AbstractEntity<?>> extends IScatterPlotRightMargin<T> {

    /**
     * Assigns the bottom margin of scatter plot.
     *
     * @param margin
     * @return
     */
    IScatterPlotRightMargin<T> bottomMargin(int margin);
}
