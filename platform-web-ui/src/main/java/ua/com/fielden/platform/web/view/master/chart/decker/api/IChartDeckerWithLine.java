package ua.com.fielden.platform.web.view.master.chart.decker.api;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IChartDeckerWithLine<T extends AbstractEntity<?>>{

    IChartDeckerLineColour<T> withLine(final String propertyName);
}
