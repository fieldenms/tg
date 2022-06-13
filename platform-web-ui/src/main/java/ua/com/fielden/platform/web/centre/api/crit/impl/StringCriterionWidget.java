package ua.com.fielden.platform.web.centre.api.crit.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.widgets.singlelinetext.impl.SinglelineTextWidget;

/**
 * An implementation for string editor criterion.
 *
 * @author TG Team
 *
 */
public class StringCriterionWidget extends AbstractMultiCriterionWidget {

    /**
     * Creates an instance of {@link StringCriterionWidget} for specified entity type and property name.
     *
     * @param criteriaType
     * @param propertyName
     */
    public StringCriterionWidget(final Class<? extends AbstractEntity<?>> root, final Class<?> managedType, final String propertyName) {
        super(root, "centre/criterion/tg-criterion", propertyName,
                new SinglelineTextWidget(
                        AbstractCriterionWidget.generateSingleTitleDesc(root, managedType, propertyName),
                        AbstractCriterionWidget.generateSingleName(root, managedType, propertyName)
                ));
    }
}
