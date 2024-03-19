package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract to configure scatter plot/
 *
 * @param <T>
 */
public interface IScatterPlotMasterBuilder<T extends AbstractEntity<?>> {

    /**
     * Start scatter plot configuration for specified entity type
     *
     * @param entityType
     * @return
     */
    IScatterPlotCategoryProperty<T> forEntity(Class<T> entityType);

    /**
     * Start scatter plot configuration for specified entity type. Also, this scatter plot master get saved when opened.
     *
     * @param entityType
     * @return
     */
    IScatterPlotCategoryProperty<T> forEntityWithSaveOnActivation(Class<T> entityType);
}
