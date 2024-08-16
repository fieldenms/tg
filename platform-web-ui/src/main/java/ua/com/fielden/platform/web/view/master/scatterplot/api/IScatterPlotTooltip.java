package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

/**
 * A contract for configuring a tooltip entry for a scatter plot data point.
 *
 * @param <T>
 *
 * @author TG Team
 */
public interface IScatterPlotTooltip<T extends AbstractEntity<?>> extends IScatterPlotLegend<T> {

    /**
     * Adds a property name of the master entity type to represent an entry in a tooltip.
     * The property title and value are displayed in a tooltip.
     * There can be several calls to this method and their order defines the order of entries in a tooltip.
     *
     * @param propertyName
     * @return
     */
    IScatterPlotTooltip<T> addPropertyToTooltip(IConvertableToPath propertyName);

}
