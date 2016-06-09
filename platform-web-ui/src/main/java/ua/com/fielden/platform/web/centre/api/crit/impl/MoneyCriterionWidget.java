package ua.com.fielden.platform.web.centre.api.crit.impl;

import ua.com.fielden.platform.web.view.master.api.widgets.money.impl.MoneyWidget;

/**
 * An implementation for money double-editor criterion.
 *
 * @author TG Team
 *
 */
public class MoneyCriterionWidget extends AbstractRangeCriterionWidget {

    /**
     * Creates an instance of {@link MoneyCriterionWidget} for specified entity type and property name.
     *
     * @param criteriaType
     * @param propertyName
     */
    public MoneyCriterionWidget(final Class<?> root, final Class<?> managedType, final String propertyName) {
        super("centre/criterion/multi/range/tg-range-criterion", propertyName,
                new MoneyWidget(
                        AbstractCriterionWidget.generateTitleDesc(root, managedType, propertyName).getKey(),
                        AbstractCriterionWidget.generateNames(root, managedType, propertyName).getKey()
                ),
                new MoneyWidget(
                        AbstractCriterionWidget.generateTitleDesc(root, managedType, propertyName).getValue(),
                        AbstractCriterionWidget.generateNames(root, managedType, propertyName).getValue()
                ));
    }
}
