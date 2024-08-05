package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract to specify a scatter plot title. A scatter plot title is optional, it can be empty or omitted completely.
 *
 * @param <T>
 *
 * @author TG Team
 */
public interface IScatterPlotTitle<T extends AbstractEntity<?>> extends IScatterPlotRangeAxisTitle<T> {

    IScatterPlotRangeAxisTitle<T> withTitle(String title);

}
