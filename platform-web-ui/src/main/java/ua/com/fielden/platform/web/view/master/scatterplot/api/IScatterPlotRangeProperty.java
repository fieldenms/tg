package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

/**
 * A contract to specify a property name and an axis boundary configuration that is used for the range axis of a scatter plot (y-axis).
 *
 * @param <T>
 *
 * @author TG Team
 */
public interface IScatterPlotRangeProperty<T extends AbstractEntity<?>> {

    /**
     * Assigns a range property name (y-axis values) and a configuration of its boundary.
     *
     * @param propertyName
     * @param rangeConfig
     * @return
     */
    IScatterPlotDomainProperty<T> configRangeProperty(IConvertableToPath propertyName, IScatterPlotAxisBoundaryConfig rangeConfig);

}
