package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;

import java.util.Map;

/**
 * A contract to specify a legend item for a scatter plot.
 *
 * @param <T>
 *
 * @author TG Team
 */
public interface IScatterPlotLegend <T extends AbstractEntity<?>> extends IScatterPlotAction<T> {

    /**
     * Adds a new legend item to a scatter plot legend.
     *
     * @param style specifies a style for corresponding data points, rendered by a scatter plot.
     * @param title a textual description of the data points rendered with the specified style.
     * @return
     */
    IScatterPlotLegend<T> addLegendItem(Map<String, String> style, String title);

}
