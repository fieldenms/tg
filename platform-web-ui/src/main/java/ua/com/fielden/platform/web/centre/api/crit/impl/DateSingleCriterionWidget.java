package ua.com.fielden.platform.web.centre.api.crit.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.widgets.datetimepicker.impl.DateTimePickerWidget;

/// An implementation for date single-editor criterion.
///
public class DateSingleCriterionWidget extends AbstractSingleCriterionWidget {

    /// Creates an instance of [DateSingleCriterionWidget] for specified entity type and property name.
    ///
    public DateSingleCriterionWidget(final Class<? extends AbstractEntity<?>> root, final Class<?> managedType, final String propertyName) {
        super(root, propertyName, new DateTimePickerWidget(
            generateSingleTitleDesc(root, managedType, propertyName),
            generateSingleName(root, managedType, propertyName),
            false
        ));
    }
}
