package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract for specifying the bottom margin of a scatter plot.
 *
 * @param <T>
 *
 * @author TG Team
 */
public interface IScatterPlotBottomMargin<T extends AbstractEntity<?>> extends IScatterPlotRightMargin<T> {

    IScatterPlotRightMargin<T> bottomMargin(int px);

}
