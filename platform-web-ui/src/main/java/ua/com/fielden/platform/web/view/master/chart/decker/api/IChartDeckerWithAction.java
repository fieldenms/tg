package ua.com.fielden.platform.web.view.master.chart.decker.api;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;

public interface IChartDeckerWithAction<T extends AbstractEntity<?>> extends IChartDeckerAlso<T>{

    IChartDeckerAlso<T> withAction(EntityActionConfig action);
}
