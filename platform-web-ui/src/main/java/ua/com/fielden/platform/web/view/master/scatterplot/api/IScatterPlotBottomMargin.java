package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IScatterPlotBottomMargin<T extends AbstractEntity<?>> extends IScatterPlotRightMargin<T> {

    IScatterPlotRightMargin<T> bottomMargin(int margin);
}
