package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Set the property name that should be used to retrieve styles for dot on scatter plot
 *
 * @param <T>
 */
public interface IScatterPlotStyleProperty<T extends AbstractEntity<?>> {

    /**
     * Assigns the property name that is used to retrieve styles for dot scatter plot.
     *
     * @param propertyName
     * @return
     */
    IScatterPlotTitle<T> setStylePropertyName(String propertyName);
}
