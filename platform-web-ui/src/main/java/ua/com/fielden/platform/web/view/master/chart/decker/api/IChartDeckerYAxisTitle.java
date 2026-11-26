package ua.com.fielden.platform.web.view.master.chart.decker.api;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IChartDeckerYAxisTitle<T extends AbstractEntity<?>> extends  IChartDeckerTopMargin<T>{

    IChartDeckerTopMargin<T> withYAxisTitle(String title);
}
