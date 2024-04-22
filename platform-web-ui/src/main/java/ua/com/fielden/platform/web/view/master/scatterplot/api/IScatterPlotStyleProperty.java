package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract to specify a property name or a key that should be used to associate styles for rendering values on a scatter plot (shapes, their size, colours, etc.).
 *
 * @param <T>
 *
 * @author TG Team
 */
public interface IScatterPlotStyleProperty<T extends AbstractEntity<?>> {

    /**
     * Assigns a key that is used to retrieve styles for scatter plot values.
     *
     * @param key
     * @return
     */
    IScatterPlotTitle<T> setStyleKey(String key);

}
