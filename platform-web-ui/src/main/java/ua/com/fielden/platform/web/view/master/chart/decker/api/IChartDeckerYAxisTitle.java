package ua.com.fielden.platform.web.view.master.chart.decker.api;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IChartDeckerYAxisTitle<T extends AbstractEntity<?>> extends  IChartDeckerAlso<T>{

    IChartDeckerAlso<T> withYAxisTitle(String title);
}
