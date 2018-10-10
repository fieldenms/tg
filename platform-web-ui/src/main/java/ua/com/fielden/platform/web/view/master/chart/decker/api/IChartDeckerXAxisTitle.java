package ua.com.fielden.platform.web.view.master.chart.decker.api;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IChartDeckerXAxisTitle<T extends AbstractEntity<?>> extends IChartDeckerYAxisTitle<T>{

    IChartDeckerYAxisTitle<T> withXAxisTitle(String title);

}
