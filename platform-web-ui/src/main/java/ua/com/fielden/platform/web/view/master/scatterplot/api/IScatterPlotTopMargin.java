package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IScatterPlotTopMargin<T extends AbstractEntity<?>> extends IScatterPlotLeftMargin<T>{

    IScatterPlotLeftMargin<T> topMargin(int margin);
}
