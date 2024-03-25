package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

/**
 * Contract to specify property name which value should be used on x-axis of scatter plot.
 *
 * @param <T>
 */
public interface IScatterPlotValueProperty<T extends AbstractEntity<?>> {

    /**
     * Specifies the property name of entity type which value should be used to as domain values for x-axis of scatter plot.
     *
     * @param propertyName
     * @return
     */
    IScatterPlotStyleProperty<T> setValuePropertyName (IConvertableToPath propertyName);
}
