package ua.com.fielden.platform.web.centre.api.crit.impl;

import java.util.Map;

import ua.com.fielden.platform.web.view.master.api.widgets.datetimepicker.impl.DateTimePickerWidget;

/**
 * An implementation for date double-editor criterion.
 *
 * @author TG Team
 *
 */
public class DateCriterionWidget extends AbstractRangeCriterionWidget {

    /**
     * Creates an instance of {@link DateCriterionWidget} for specified entity type and property name.
     *
     * @param criteriaType
     * @param propertyName
     */
    public DateCriterionWidget(final Class<?> root, final Class<?> managedType, final String propertyName) {
        super("centre/criterion/multi/range/tg-date-range-criterion", propertyName,
                new DateTimePickerWidget(
                        AbstractCriterionWidget.generateTitleDesc(root, managedType, propertyName).getKey(),
                        AbstractCriterionWidget.generateNames(root, managedType, propertyName).getKey()
                ),
                new DateTimePickerWidget(
                        AbstractCriterionWidget.generateTitleDesc(root, managedType, propertyName).getValue(),
                        AbstractCriterionWidget.generateNames(root, managedType, propertyName).getValue()
                ));
    }

    @Override
    protected Map<String, Object> createCustomAttributes() {
        final Map<String, Object> attrs = super.createCustomAttributes();
        attrs.put("date-prefix", "[[propertyModel." + this.propertyName() + ".datePrefix]]");
        attrs.put("date-mnemonic", "[[propertyModel." + this.propertyName() + ".dateMnemonic]]");
        attrs.put("and-before", "[[propertyModel." + this.propertyName() + ".andBefore]]");
        return attrs;
    }
}
