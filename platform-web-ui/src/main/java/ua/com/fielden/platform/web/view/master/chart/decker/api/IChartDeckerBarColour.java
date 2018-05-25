package ua.com.fielden.platform.web.view.master.chart.decker.api;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.types.Colour;

public interface IChartDeckerBarColour<T extends AbstractEntity<?>> extends IChartDeckerWithAction<T>{

    IChartDeckerWithAction<T> withBarColour(Colour barColour);
}
