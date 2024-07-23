package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

/**
 * A contract to specify property name and axis boundary configuration that is used to configure the domain axis for scatter plot (x-axis).
 *
 * @param <T>
 *
 * @author TG Team
 */
public interface IScatterPlotDomainProperty<T extends AbstractEntity<?>> {

    /**
     * Assigns property name and configuration of x-axis range
     *
     * @param propertyName
     * @return
     */
    IScatterPlotStyleProperty<T> configDomainProperty(IConvertableToPath propertyName, IScatterPlotAxisBoundaryConfig axisBoundaryConfig);
}
