package ua.com.fielden.platform.web.centre.api.crit.impl;

import org.apache.commons.lang3.StringUtils;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.widgets.EntityMultiCritAutocompletionWidget;

import java.util.List;

/// An implementation for entity multi criterion.
///
public class EntityCriterionWidget extends AbstractCriterionWidget {

    /// Creates an instance of [EntityCriterionWidget] for specified entity type and property name.
    ///
    public EntityCriterionWidget(final Class<? extends AbstractEntity<?>> root, final Class<?> managedType, final String propertyName, final List<Pair<String, Boolean>> additionalProps, final CentreContextConfig centreContextConfig) {
        super(root, "centre/criterion/tg-criterion", propertyName,
                new EntityMultiCritAutocompletionWidget(
                        AbstractCriterionWidget.generateSingleTitleDesc(root, managedType, propertyName),
                        AbstractCriterionWidget.generateSingleName(root, managedType, propertyName),
                        StringUtils.isEmpty(propertyName) ? (Class<? extends AbstractEntity<?>>) root : (Class<? extends AbstractEntity<?>>) PropertyTypeDeterminator.determinePropertyType(root, propertyName),
                        centreContextConfig
                ).setAdditionalProps(additionalProps));

    }
}
