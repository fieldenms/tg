package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract to specify a title for the range axis (y-axis) of a scatter plot.
 *
 * @param <T>
 *
 * @author TG Team
 */
public interface IScatterPlotRangeAxisTitle<T extends AbstractEntity<?>> extends IScatterPlotDomainAxisTitle<T> {

    IScatterPlotRangeAxisTitle<T> withRangeAxisTitle(String title);

}
