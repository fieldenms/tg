package ua.com.fielden.platform.web.view.master.chart.decker.api;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IChartDeckerMasterBuilder<T extends AbstractEntity<?>> {

    IChartDeckerGroup<T> forEntity(Class<T> entityType);

    IChartDeckerGroup<T> forEntityWithSaveOnActivation(Class<T> entityType);

}
