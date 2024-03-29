package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IScatterPlotLeftMargin<T extends AbstractEntity<?>> extends IScatterPlotBottomMargin<T> {

    IScatterPlotBottomMargin<T> leftMargin(int margin);
}
