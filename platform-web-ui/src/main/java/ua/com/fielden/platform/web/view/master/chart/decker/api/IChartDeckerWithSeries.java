package ua.com.fielden.platform.web.view.master.chart.decker.api;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IChartDeckerWithSeries<T extends AbstractEntity<?>>{

    IChartDeckerWithSeriesTitle<T> withSeries(String propertyName);
}
