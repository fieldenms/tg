package ua.com.fielden.platform.web.view.master.chart.decker.api;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IChartDeckerAddDeck<T extends AbstractEntity<?>> {

    IChartDeckerWithTitle<T> addDeckForProperty(Class<? extends AbstractEntity<?>> entityType, String aggregationProperty);
}
