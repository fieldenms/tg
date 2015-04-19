package ua.com.fielden.platform.web.centre.api.crit.impl;

import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.autocompleter.impl.EntityAutocompletionWidget;

/**
 * An implementation for entity crit-only single criterion.
 *
 * @author TG Team
 *
 */
public class EntitySingleCriterionWidget extends AbstractSingleCriterionWidget {

    /**
     * Creates an instance of {@link EntitySingleCriterionWidget} for specified entity type and property name.
     *
     * @param criteriaType
     * @param propertyName
     */
    public EntitySingleCriterionWidget(final Class<?> root, final Class<?> managedType, final String propertyName, final CentreContextConfig centreContextConfig) {
        super(propertyName,
                new EntityAutocompletionWidget(
                        AbstractCriterionWidget.generateSingleTitleDesc(root, managedType, propertyName),
                        AbstractCriterionWidget.generateSingleName(root, managedType, propertyName),
                        centreContextConfig
                ));
    }
}
