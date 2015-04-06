package ua.com.fielden.platform.web.centre.api.crit.impl;

import ua.com.fielden.platform.web.view.master.api.widgets.decimal.impl.DecimalWidget;

/**
 * An implementation for decimal single-editor criterion.
 *
 * @author TG Team
 *
 */
public class DecimalSingleCriterionWidget extends AbstractCriterionWidget {

    /**
     * Creates an instance of {@link DecimalSingleCriterionWidget} for specified entity type and property name.
     *
     * @param criteriaType
     * @param propertyName
     */
    public DecimalSingleCriterionWidget(final Class<?> root, final Class<?> managedType, final String propertyName) {
        super("centre/criterion/tg-criterion", propertyName,
                new DecimalWidget(
                        AbstractCriterionWidget.generateSingleTitleDesc(root, managedType, propertyName),
                        AbstractCriterionWidget.generateSingleName(root, managedType, propertyName)
                ));
    }
}
