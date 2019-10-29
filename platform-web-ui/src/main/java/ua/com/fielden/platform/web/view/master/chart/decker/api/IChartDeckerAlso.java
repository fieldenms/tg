package ua.com.fielden.platform.web.view.master.chart.decker.api;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IChartDeckerAlso<T extends AbstractEntity<?>> extends IChartDeckerMasterDone<T>{

    IChartDeckerAddDeck<T> also();
}
