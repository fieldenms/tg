package ua.com.fielden.platform.web.view.master.chart.decker.api;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IChartDeckerWithTitle<T extends AbstractEntity<?>> extends IChartDeckerXAxisTitle<T>{

    IChartDeckerXAxisTitle<T> withTitle(String title);
}
