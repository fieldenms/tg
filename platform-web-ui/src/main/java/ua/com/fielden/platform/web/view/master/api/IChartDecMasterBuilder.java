package ua.com.fielden.platform.web.view.master.api;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IChartDecMasterBuilder<T extends AbstractEntity<?>> {

    IChartDecMasterDone<T> forEntity(Class<T> entityType);

    IChartDecMasterDone<T> forEntityWithSaveOnActivation(Class<T> entityType);

}
