package ua.com.fielden.platform.web.view.master.chart.decker.api;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IChartDeckerWithSeriesTitle<T extends AbstractEntity<?>> extends IChartDeckerWithSeriesAction<T>{

    IChartDeckerWithSeriesAction<T> title(String title);
}
