package ua.com.fielden.platform.web.view.master.chart.decker.api;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.IMaster;

public interface IChartDeckerMasterDone<T extends AbstractEntity<?>> {

    IMaster<T> done();
}
