package ua.com.fielden.platform.web.view.master.chart.decker.api;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

public interface IChartDeckerGroup<T extends AbstractEntity<?>> {

    IChartDeckerDesc<T> groupKeyProp(final String property);

    default IChartDeckerDesc<T> groupKeyProp(final IConvertableToPath property) {
        return groupKeyProp(property.toPath());
    }

}