package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;

/**
 * A contract for specifying a tap/double-click action for data points in a scatter plot.
 *
 * @param <T>
 *
 * @author TG Team
 */
public interface IScatterPlotAction<T extends AbstractEntity<?>> extends IScatterPlotDone<T> {

    /**
     * Assigns an action for a scatter plot.
     *
     * @param action
     * @return
     */
    IScatterPlotDone<T> withAction(EntityActionConfig action);

}
