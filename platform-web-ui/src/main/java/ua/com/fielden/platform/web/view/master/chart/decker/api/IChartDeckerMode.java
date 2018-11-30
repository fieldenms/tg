package ua.com.fielden.platform.web.view.master.chart.decker.api;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IChartDeckerMode<T extends AbstractEntity<?>> extends IChartDeckerWithTitle<T> {

    IChartDeckerWithTitle<T> mode(BarMode mode);
}
