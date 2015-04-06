package ua.com.fielden.platform.web.centre.api.crit.impl;

import ua.com.fielden.platform.web.view.master.api.widgets.autocompleter.impl.EntityAutocompletionWidget;

/**
 * An implementation for entity crit-only single criterion.
 *
 * @author TG Team
 *
 */
public class EntitySingleCriterionWidget extends AbstractCriterionWidget {

    /**
     * Creates an instance of {@link EntitySingleCriterionWidget} for specified entity type and property name.
     *
     * @param criteriaType
     * @param propertyName
     */
    public EntitySingleCriterionWidget(final Class<?> root, final Class<?> managedType, final String propertyName) {
        super("centre/criterion/tg-criterion", propertyName,
                new EntityAutocompletionWidget(
                        AbstractCriterionWidget.generateSingleTitleDesc(root, managedType, propertyName),
                        AbstractCriterionWidget.generateSingleName(root, managedType, propertyName)
                ));
    }
}
