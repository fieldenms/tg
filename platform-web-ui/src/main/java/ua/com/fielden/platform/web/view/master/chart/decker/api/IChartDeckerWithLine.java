package ua.com.fielden.platform.web.view.master.chart.decker.api;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

public interface IChartDeckerWithLine<T extends AbstractEntity<?>>{

    IChartDeckerLineColour<T> withLine(final String propertyName);

    default IChartDeckerLineColour<T> withLine(final IConvertableToPath propertyName) {
        return withLine(propertyName.toPath());
    }

}