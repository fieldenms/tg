package ua.com.fielden.platform.web.view.master.chart.decker.api;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.types.Colour;

public interface IChartDeckerLineColour<T extends AbstractEntity<?>> {

    IChartDeckerLineTitle<T> colour(Colour colour);
}
