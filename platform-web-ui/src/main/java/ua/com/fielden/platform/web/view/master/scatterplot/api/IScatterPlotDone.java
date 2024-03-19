package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.IMaster;

/**
 * Contract to identify the end of scatter plot configuration.
 *
 * @param <T>
 */
public interface IScatterPlotDone<T extends AbstractEntity<?>> {

    /**
     * Indicates the end of scatter plot configuration.
     *
     * @return
     */
    IMaster<T> done();
}
