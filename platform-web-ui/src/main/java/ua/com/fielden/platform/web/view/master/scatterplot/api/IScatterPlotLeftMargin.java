package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract for specifying the left margin of a scatter plot.
 *
 * @param <T>
 *
 * @author TG Team
 */
public interface IScatterPlotLeftMargin<T extends AbstractEntity<?>> extends IScatterPlotBottomMargin<T> {

    IScatterPlotBottomMargin<T> leftMargin(int px);
}
