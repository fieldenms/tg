package ua.com.fielden.platform.web.view.master.chart.decker.api;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IChartDeckerGroup<T extends AbstractEntity<?>> {

    IChartDeckerDesc<T> groupKeyProp(final String property);

}
