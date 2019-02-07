package ua.com.fielden.platform.web.view.master.chart.decker.api;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IChartDeckerLineTitle<T extends AbstractEntity<?>> extends IChartDeckerLineAlso<T> {

    IChartDeckerLineAlso<T> title(String title);
}
