package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

/**
 * Contract to specify property name and axis range configuration that will be used to configure category axis of scatter plot
 *
 * @param <T>
 */
public interface IScatterPlotCategoryProperty<T extends AbstractEntity<?>> {

    /**
     * Assigns category property name (y-axis values) and configuration of it's range
     *
     * @param propertyName
     * @return
     */
    IScatterPlotValueProperty<T> configCategoryProperty(IConvertableToPath propertyName, IScatterPlotRangeConfig rangeConfig);
}
