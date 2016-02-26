package ua.com.fielden.platform.web.centre.api.crit.impl;

import ua.com.fielden.platform.web.view.master.api.widgets.decimal.impl.DecimalWidget;

/**
 * An implementation for decimal double-editor criterion.
 *
 * @author TG Team
 *
 */
public class DecimalCriterionWidget extends AbstractRangeCriterionWidget {

    /**
     * Creates an instance of {@link DecimalCriterionWidget} for specified entity type and property name.
     *
     * @param criteriaType
     * @param propertyName
     */
    public DecimalCriterionWidget(final Class<?> root, final Class<?> managedType, final String propertyName) {
        super("centre/criterion/multi/range/tg-range-criterion", propertyName,
                new DecimalWidget(
                        AbstractCriterionWidget.generateTitleDesc(root, managedType, propertyName).getKey(),
                        AbstractCriterionWidget.generateNames(root, managedType, propertyName).getKey()
                ),
                new DecimalWidget(
                        AbstractCriterionWidget.generateTitleDesc(root, managedType, propertyName).getValue(),
                        AbstractCriterionWidget.generateNames(root, managedType, propertyName).getValue()
                ));
    }
}
