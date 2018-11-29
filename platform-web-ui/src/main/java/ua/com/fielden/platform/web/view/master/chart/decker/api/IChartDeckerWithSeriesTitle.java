package ua.com.fielden.platform.web.view.master.chart.decker.api;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IChartDeckerWithSeriesTitle<T extends AbstractEntity<?>> extends IChartDeckerWithSeriesColour<T>{

    IChartDeckerWithSeriesColour<T> title(String title);
}
