package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Allows developer to specify scatter plot title.
 *
 * @param <T>
 */
public interface IScatterPlotTitle<T extends AbstractEntity<?>> extends IScatterPlotXAxisTitle<T>{

    /**
     * Assigns the title of scatter plot
     *
     * @param title
     * @return
     */
    IScatterPlotXAxisTitle<T> withTitle (String title);
}
