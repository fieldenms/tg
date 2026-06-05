package ua.com.fielden.platform.web.centre.api.crit.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.widgets.EntityMultiCritAutocompletionWidget;

import java.util.List;

/// An implementation for entity criterion bound to a string-typed property.
///
public class EntityStringCriterionWidget extends AbstractCriterionWidget {

    public EntityStringCriterionWidget(
            final Class<? extends AbstractEntity<?>> root,
            final Class<?> managedType,
            final String propertyName,
            final Class<? extends AbstractEntity<?>> propertyType,
            final List<Pair<String, Boolean>> additionalProps,
            final CentreContextConfig centreContextConfig)
    {
        super(root, "centre/criterion/tg-criterion", propertyName,
              new EntityMultiCritAutocompletionWidget(
                      AbstractCriterionWidget.generateSingleTitleDesc(root, managedType, propertyName),
                      AbstractCriterionWidget.generateSingleName(root, managedType, propertyName),
                      propertyType,
                      centreContextConfig
              ).setAdditionalProps(additionalProps));
    }
}
