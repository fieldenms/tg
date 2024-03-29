package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IScatterPlotRightMargin<T extends AbstractEntity<?>> extends IScatterPlotTooltip<T> {

    IScatterPlotTooltip<T> rightMargin(int margin);
}
