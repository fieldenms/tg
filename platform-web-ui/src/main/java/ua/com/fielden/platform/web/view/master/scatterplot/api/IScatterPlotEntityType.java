package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Contract to specify the type of entities displayed on scatter plot.
 *
 * @param <T>
 *
 * @author TG Team
 */
public interface IScatterPlotEntityType<T extends AbstractEntity<?>> {

    /**
     * Specifies the type of entities displayed on scatter plot.
     *
     * @param entityType
     * @return
     */
    IScatterPlotRangeProperty<T> setChartEntityType (Class<? extends AbstractEntity<?>> entityType);
}
