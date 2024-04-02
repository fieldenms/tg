package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

/**
 * Set the property name or key that should be used to retrieve styles for scatter plot dots
 *
 * @param <T>
 *
 * @author TG Team
 */
public interface IScatterPlotStyleProperty<T extends AbstractEntity<?>> {

    /**
     * Assigns the property name that is used to retrieve styles for scatter plot dots.
     *
     * @param propertyName
     * @return
     */
    IScatterPlotTitle<T> setStylePropertyName(IConvertableToPath propertyName);

    /**
     * Assigns the key that is used to retrieve styles for scatter plot dots.
     *
     * @param propertyName
     * @return
     */
    IScatterPlotTitle<T> setStylePropertyName(String propertyName);
}
