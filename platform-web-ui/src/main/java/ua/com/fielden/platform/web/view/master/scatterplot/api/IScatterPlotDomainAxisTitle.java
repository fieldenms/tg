package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract to specify a title for the domain axis (x-axis) of a scatter plot.
 *
 * @param <T>
 *
 * @author TG Team
 */
public interface IScatterPlotDomainAxisTitle<T extends AbstractEntity<?>> extends IScatterPlotTopMargin<T> {

    IScatterPlotTopMargin<T> withDomainAxisTitle(String title);

}
