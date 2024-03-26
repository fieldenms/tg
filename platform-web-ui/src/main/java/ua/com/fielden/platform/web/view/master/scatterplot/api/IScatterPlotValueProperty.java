package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

/**
 * Contract to specify property name and axis range configuration that should be used to configure continuous axis of scatter plot (usually x-axis).
 *
 * @param <T>
 */
public interface IScatterPlotValueProperty<T extends AbstractEntity<?>> {

    /**
     * Assigns property name (x-axis) and configuration of it's range
     *
     * @param propertyName
     * @return
     */
    IScatterPlotStyleProperty<T> configValueProperty (IConvertableToPath propertyName, IScatterPlotRangeConfig rangeConfig);
}
