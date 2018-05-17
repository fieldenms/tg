package ua.com.fielden.platform.web.view.master.api;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IChartDecMasterDone<T extends AbstractEntity<?>> {

    IMaster<T> done();
}
