package ua.com.fielden.platform.web.view.master.chart.decker.api;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

public interface IChartDeckerWithSeries<T extends AbstractEntity<?>>{

    IChartDeckerWithSeriesColour<T> withSeries(String propertyName);

    default IChartDeckerWithSeriesColour<T> withSeries(final IConvertableToPath propertyName) {
        return withSeries(propertyName.toPath());
    }

}