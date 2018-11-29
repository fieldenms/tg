package ua.com.fielden.platform.web.view.master.chart.decker.api;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.types.Colour;

public interface IChartDeckerWithSeriesColour<T extends AbstractEntity<?>> {

    IChartDeckerWithSeriesAction<T> colour(Colour colour);
}
