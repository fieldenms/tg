package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;

import java.util.Map;

/**
 * Contract to specify legend for scatter plot
 *
 * @param <T>
 *
 * @author TG Team
 */
public interface IScatterPlotLegend <T extends AbstractEntity<?>> extends IScatterPlotAction<T> {

    /**
     * Adds new item to scatter plot legend
     *
     * @param style
     * @param title
     * @return
     */
    IScatterPlotLegend<T> addLegendItem(Map<String, String> style, String title);
}
