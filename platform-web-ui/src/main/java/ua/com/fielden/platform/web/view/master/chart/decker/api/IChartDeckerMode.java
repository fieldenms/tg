package ua.com.fielden.platform.web.view.master.chart.decker.api;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IChartDeckerMode<T extends AbstractEntity<?>> extends IChartDeckerShowLegend<T> {

    IChartDeckerShowLegend<T> mode(BarMode mode);
}
