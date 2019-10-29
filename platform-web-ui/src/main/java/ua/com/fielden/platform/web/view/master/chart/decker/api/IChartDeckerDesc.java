package ua.com.fielden.platform.web.view.master.chart.decker.api;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IChartDeckerDesc<T extends AbstractEntity<?>> extends IChartDeckerAddDeck<T> {

    IChartDeckerAddDeck<T> groupDescProp(String descriptionProperty);
}
