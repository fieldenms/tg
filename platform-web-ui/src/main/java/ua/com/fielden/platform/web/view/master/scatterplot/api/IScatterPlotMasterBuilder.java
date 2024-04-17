package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract to start a configuration for a scatter plot.
 *
 * @param <T>
 *
 * @author TG Team
 */
public interface IScatterPlotMasterBuilder<T extends AbstractEntity<?>> {

    /**
     * Starts a scatter plot configuration for a specified master entity type that gets executed/saved upon opening.
     * This execution/saving is useful if one needs to do some computation before the data can be rendered.
     * For example, to compute the values for the range axis (OY), which currently only supports categorical values.
     *
     * @param entityType
     * @return
     */
    IScatterPlotEntityType<T> forEntityWithSaveOnActivation(Class<T> entityType);

}
