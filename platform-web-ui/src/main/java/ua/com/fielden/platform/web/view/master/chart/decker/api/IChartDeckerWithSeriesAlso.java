package ua.com.fielden.platform.web.view.master.chart.decker.api;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IChartDeckerWithSeriesAlso<T extends AbstractEntity<?>> extends IChartDeckerAlso<T>, IChartDeckerWithLine<T>, IChartDeckerWithSeries<T>{

}
