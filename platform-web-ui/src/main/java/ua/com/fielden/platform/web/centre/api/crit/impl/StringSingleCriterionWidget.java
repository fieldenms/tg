package ua.com.fielden.platform.web.centre.api.crit.impl;

import ua.com.fielden.platform.web.view.master.api.widgets.singlelinetext.impl.SinglelineTextWidget;

/**
 * An implementation for string single-editor criterion.
 *
 * @author TG Team
 *
 */
public class StringSingleCriterionWidget extends AbstractCriterionWidget {

    /**
     * Creates an instance of {@link StringSingleCriterionWidget} for specified entity type and property name.
     *
     * @param criteriaType
     * @param propertyName
     */
    public StringSingleCriterionWidget(final Class<?> root, final Class<?> managedType, final String propertyName) {
        super("centre/criterion/tg-criterion", propertyName,
                new SinglelineTextWidget(
                        AbstractCriterionWidget.generateSingleTitleDesc(root, managedType, propertyName),
                        AbstractCriterionWidget.generateSingleName(root, managedType, propertyName)
                ));
    }
}
