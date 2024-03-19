package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract for configuring tooltip on scatter plot
 *
 * @param <T>
 */
public interface IScatterPlotTooltip<T extends AbstractEntity<?>> extends IScatterPlotLegend<T> {

    /**
     * Adds a property name of entity type which value and title will be displayed in tooltip.
     *
     * @param propertyName
     * @return
     */
    IScatterPlotTooltip<T> addPropertyToTooltip(String propertyName);
}
