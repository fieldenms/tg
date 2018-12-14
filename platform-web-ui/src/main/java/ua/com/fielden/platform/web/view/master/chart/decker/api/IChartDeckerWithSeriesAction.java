package ua.com.fielden.platform.web.view.master.chart.decker.api;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;

public interface IChartDeckerWithSeriesAction<T extends AbstractEntity<?>> extends IChartDeckerWithSeriesAlso<T>{

    IChartDeckerWithSeriesAlso<T> action(EntityActionConfig action);
}
