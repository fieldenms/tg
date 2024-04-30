package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract to specify a type of entities that are displayed on a scatter plot.
 *
 * @param <T>
 *
 * @author TG Team
 */
public interface IScatterPlotEntityType<T extends AbstractEntity<?>> {

    /**
     * Specifies a type of entities displayed on a scatter plot.
     *
     * @param entityType
     * @return
     */
    IScatterPlotRangeProperty<T> setChartEntityType(Class<? extends AbstractEntity<?>> entityType);

}
