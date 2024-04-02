package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

/**
 * Contract to specify property name and axis range configuration that should be used to configure continuous axis of scatter plot (x-axis).
 *
 * @param <T>
 *
 * @author TG Team
 */
public interface IScatterPlotValueProperty<T extends AbstractEntity<?>> {

    /**
     * Assigns property name and configuration of x-axis range
     *
     * @param propertyName
     * @return
     */
    IScatterPlotStyleProperty<T> configValueProperty (IConvertableToPath propertyName, IScatterPlotRangeConfig rangeConfig);
}
