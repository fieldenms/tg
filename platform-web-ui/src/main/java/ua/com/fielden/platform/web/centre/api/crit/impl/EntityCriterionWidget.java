package ua.com.fielden.platform.web.centre.api.crit.impl;

import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.widgets.EntityCritAutocompletionWidget;

/**
 * An implementation for entity multi criterion.
 *
 * @author TG Team
 *
 */
public class EntityCriterionWidget extends AbstractMultiCriterionWidget {

    /**
     * Creates an instance of {@link EntityCriterionWidget} for specified entity type and property name.
     *
     * @param criteriaType
     * @param propertyName
     */
    public EntityCriterionWidget(final Class<?> root, final Class<?> managedType, final String propertyName, final CentreContextConfig centreContextConfig) {
        super("centre/criterion/multi/tg-multi-criterion", propertyName,
                new EntityCritAutocompletionWidget(
                        AbstractCriterionWidget.generateSingleTitleDesc(root, managedType, propertyName),
                        AbstractCriterionWidget.generateSingleName(root, managedType, propertyName),
                        centreContextConfig
                ));
    }
}
