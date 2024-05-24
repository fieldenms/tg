package ua.com.fielden.platform.web.centre.api.crit.impl;

import static ua.com.fielden.platform.web.centre.WebApiUtils.webComponent;

import ua.com.fielden.platform.entity.AbstractEntity;
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
    public DecimalCriterionWidget(final Class<? extends AbstractEntity<?>> root, final Class<?> managedType, final String propertyName) {
        super(root, webComponent("centre/criterion/multi/range/tg-range-criterion"), propertyName,
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
