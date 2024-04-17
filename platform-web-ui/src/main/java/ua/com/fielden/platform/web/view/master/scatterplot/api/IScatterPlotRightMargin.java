package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract for specifying the right margin of a scatter plot.
 *
 * @param <T>
 *
 * @author TG Team
 */
public interface IScatterPlotRightMargin<T extends AbstractEntity<?>> extends IScatterPlotTooltip<T> {

    IScatterPlotTooltip<T> rightMargin(int px);
}
