package ua.com.fielden.platform.web.centre.api.crit.impl;

import ua.com.fielden.platform.web.view.master.api.widgets.spinner.impl.SpinnerWidget;

/**
 * An implementation for integer (or long) double-editor criterion.
 *
 * @author TG Team
 *
 */
public class IntegerCriterionWidget extends AbstractRangeCriterionWidget {

    /**
     * Creates an instance of {@link IntegerCriterionWidget} for specified entity type and property name.
     *
     * @param criteriaType
     * @param propertyName
     */
    public IntegerCriterionWidget(final Class<?> root, final Class<?> managedType, final String propertyName) {
        super("centre/criterion/multi/range/tg-range-criterion", propertyName,
                new SpinnerWidget(
                        AbstractCriterionWidget.generateTitleDesc(root, managedType, propertyName).getKey(),
                        AbstractCriterionWidget.generateNames(root, managedType, propertyName).getKey()
                ),
                new SpinnerWidget(
                        AbstractCriterionWidget.generateTitleDesc(root, managedType, propertyName).getValue(),
                        AbstractCriterionWidget.generateNames(root, managedType, propertyName).getValue()
                ));
    }
}
