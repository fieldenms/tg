package ua.com.fielden.platform.web.centre.api.crit.impl;

import ua.com.fielden.platform.web.view.master.api.widgets.spinner.impl.SpinnerWidget;

/**
 * An implementation for integer (or long) single-editor criterion.
 *
 * @author TG Team
 *
 */
public class IntegerSingleCriterionWidget extends AbstractSingleCriterionWidget {

    /**
     * Creates an instance of {@link IntegerSingleCriterionWidget} for specified entity type and property name.
     *
     * @param criteriaType
     * @param propertyName
     */
    public IntegerSingleCriterionWidget(final Class<?> root, final Class<?> managedType, final String propertyName) {
        super(propertyName,
                new SpinnerWidget(
                        AbstractCriterionWidget.generateSingleTitleDesc(root, managedType, propertyName),
                        AbstractCriterionWidget.generateSingleName(root, managedType, propertyName)
                ));
    }
}
