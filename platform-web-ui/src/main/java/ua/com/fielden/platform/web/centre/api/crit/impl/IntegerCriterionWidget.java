package ua.com.fielden.platform.web.centre.api.crit.impl;

import static ua.com.fielden.platform.web.centre.WebApiUtils.webComponent;

import ua.com.fielden.platform.entity.AbstractEntity;
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
    public IntegerCriterionWidget(final Class<? extends AbstractEntity<?>> root, final Class<?> managedType, final String propertyName) {
        super(root, webComponent("centre/criterion/multi/range/tg-range-criterion"), propertyName,
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
