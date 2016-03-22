package ua.com.fielden.platform.web.centre.api.crit.impl;

import ua.com.fielden.platform.web.view.master.api.widgets.money.impl.MoneyWidget;

/**
 * An implementation for money single-editor criterion.
 *
 * @author TG Team
 *
 */
public class MoneySingleCriterionWidget extends AbstractSingleCriterionWidget {

    /**
     * Creates an instance of {@link MoneySingleCriterionWidget} for specified entity type and property name.
     *
     * @param criteriaType
     * @param propertyName
     */
    public MoneySingleCriterionWidget(final Class<?> root, final Class<?> managedType, final String propertyName) {
        super(propertyName,
                new MoneyWidget(
                        AbstractCriterionWidget.generateSingleTitleDesc(root, managedType, propertyName),
                        AbstractCriterionWidget.generateSingleName(root, managedType, propertyName)
                ));
    }
}
