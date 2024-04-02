package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;

/**
 * Contract for assigning action to scatter plot on double-click event
 * @param <T>
 *
 * @author TG Team
 */
public interface IScatterPlotAction<T extends AbstractEntity<?>> extends IScatterPlotDone<T>{

    /**
     * Assigns action to scatter plot.
     *
     * @param action
     * @return
     */
    IScatterPlotDone<T> withAction(EntityActionConfig action);
}
