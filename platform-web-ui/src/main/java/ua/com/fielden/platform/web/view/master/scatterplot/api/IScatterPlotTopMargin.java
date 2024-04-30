package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract for specifying the top margin of a scatter plot.
 *
 * @param <T>
 *
 * @author TG Team
 */
public interface IScatterPlotTopMargin<T extends AbstractEntity<?>> extends IScatterPlotLeftMargin<T>{

    IScatterPlotLeftMargin<T> topMargin(int px);

}
