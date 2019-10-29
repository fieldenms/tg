package ua.com.fielden.platform.web.view.master.chart.decker.api;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IChartDeckerYAxisTitle<T extends AbstractEntity<?>> extends  IChartDeckerWithSeries<T>{

    IChartDeckerWithSeries<T> withYAxisTitle(String title);
}
