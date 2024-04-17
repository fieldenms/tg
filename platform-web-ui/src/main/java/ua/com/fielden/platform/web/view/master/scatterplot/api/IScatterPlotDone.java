package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.IMaster;

/**
 * A contract to complete a scatter plot configuration.
 *
 * @param <T>
 *
 * @author TG Team
 */
public interface IScatterPlotDone<T extends AbstractEntity<?>> {

    /**
     * Indicates the end of a scatter plot configuration.
     *
     * @return
     */
    IMaster<T> done();

}
