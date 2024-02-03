package ua.com.fielden.platform.web.view.master.chart.decker.api;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

public interface IChartDeckerDesc<T extends AbstractEntity<?>> extends IChartDeckerAddDeck<T> {

    IChartDeckerAddDeck<T> groupDescProp(final String descriptionProperty);

    default IChartDeckerAddDeck<T> groupDescProp(final IConvertableToPath descriptionProperty) {
        return groupDescProp(descriptionProperty.toPath());
    }

}