package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Contract to specify property name that will be used as category for scatter plot
 *
 * @param <T>
 */
public interface IScatterPlotCategoryProperty<T extends AbstractEntity<?>> {

    /**
     * Assigns category  property name (y-axis values)
     *
     * @param propertyName
     * @return
     */
    IScatterPlotValueProperty<T> setCategoryProopertyName(String propertyName);
}
