package ua.com.fielden.platform.web.centre.api.crit.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.widgets.singlelinetext.impl.SinglelineTextWidget;

/// An implementation for a criterion bound to a string-typed property.
///
public class StringCriterionWidget extends AbstractCriterionWidget {

    public StringCriterionWidget(final Class<? extends AbstractEntity<?>> root, final Class<?> managedType, final String propertyName) {
        super(root, "centre/criterion/tg-criterion", propertyName,
              new SinglelineTextWidget(AbstractCriterionWidget.generateSingleTitleDesc(root, managedType, propertyName),
                                       AbstractCriterionWidget.generateSingleName(root, managedType, propertyName)));
    }

}
