package ua.com.fielden.platform.web.centre.api.crit.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.widgets.checkbox.impl.CheckboxWidget;

/**
 * An implementation for boolean single-editor criterion.
 *
 * @author TG Team
 *
 */
public class BooleanSingleCriterionWidget extends AbstractSingleCriterionWidget {

    /**
     * Creates an instance of {@link BooleanSingleCriterionWidget} for specified entity type and property name.
     *
     * @param criteriaType
     * @param propertyName
     */
    public BooleanSingleCriterionWidget(final Class<? extends AbstractEntity<?>> root, final Class<?> managedType, final String propertyName) {
        super(root, propertyName,
                new CheckboxWidget(
                        AbstractCriterionWidget.generateSingleTitleDesc(root, managedType, propertyName),
                        AbstractCriterionWidget.generateSingleName(root, managedType, propertyName)
                ));
    }
}
