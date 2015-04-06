package ua.com.fielden.platform.web.centre.api.crit.impl;

import ua.com.fielden.platform.web.view.master.api.widgets.checkbox.impl.CheckboxWidget;

/**
 * An implementation for boolean double-editor criterion.
 *
 * @author TG Team
 *
 */
public class BooleanCriterionWidget extends AbstractCriterionWidget {

    /**
     * Creates an instance of {@link BooleanCriterionWidget} for specified entity type and property name.
     *
     * @param criteriaType
     * @param propertyName
     */
    public BooleanCriterionWidget(final Class<?> root, final Class<?> managedType, final String propertyName) {
        super("centre/criterion/tg-boolean-criterion", propertyName,
                new CheckboxWidget(
                        AbstractCriterionWidget.generateTitleDesc(root, managedType, propertyName).getKey(),
                        AbstractCriterionWidget.generateNames(root, managedType, propertyName).getKey()
                ),
                new CheckboxWidget(
                        AbstractCriterionWidget.generateTitleDesc(root, managedType, propertyName).getValue(),
                        AbstractCriterionWidget.generateNames(root, managedType, propertyName).getValue()
                ));
    }
}
