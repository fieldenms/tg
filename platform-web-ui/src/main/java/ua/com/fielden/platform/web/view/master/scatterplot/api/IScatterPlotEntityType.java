package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Contract to specify the type of entities displayed on scatter plot.
 *
 * @param <T>
 */
public interface IScatterPlotEntityType<T extends AbstractEntity<?>> {

    IScatterPlotCategoryProperty<T> setChartEntityType (Class<? extends AbstractEntity<?>> entityType);
}
