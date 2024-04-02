package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract to configure scatter plot/
 *
 * @param <T>
 *
 * @author TG Team
 */
public interface IScatterPlotMasterBuilder<T extends AbstractEntity<?>> {

    /**
     * Start scatter plot configuration for specified master entity type
     *
     * @param entityType
     * @return
     */
    IScatterPlotEntityType<T> forEntity(Class<T> entityType);

    /**
     * Start scatter plot configuration for specified master entity type. Also, this scatter plot master get saved when opened.
     *
     * @param entityType
     * @return
     */
    IScatterPlotEntityType<T> forEntityWithSaveOnActivation(Class<T> entityType);
}
