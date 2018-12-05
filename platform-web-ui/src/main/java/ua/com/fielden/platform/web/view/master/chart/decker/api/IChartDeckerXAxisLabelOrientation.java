package ua.com.fielden.platform.web.view.master.chart.decker.api;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IChartDeckerXAxisLabelOrientation<T extends AbstractEntity<?>> extends IChartDeckerYAxisTitle<T>{

    IChartDeckerYAxisTitle<T> withXAxisLabelOrientation(final LabelOrientation orientation);
}
