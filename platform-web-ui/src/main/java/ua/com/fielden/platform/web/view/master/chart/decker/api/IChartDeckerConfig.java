package ua.com.fielden.platform.web.view.master.chart.decker.api;

import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.chart.decker.api.impl.ChartDeck;

public interface IChartDeckerConfig<T extends AbstractEntity<?>> {

    Class<T> getEntityType();

    boolean shouldSaveOnActivation();

    String getGroupPropoerty();

    List<ChartDeck<T>> getDecs();
}
