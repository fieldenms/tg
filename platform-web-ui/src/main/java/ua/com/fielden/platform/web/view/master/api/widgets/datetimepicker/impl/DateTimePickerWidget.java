package ua.com.fielden.platform.web.view.master.api.widgets.datetimepicker.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

/**
 * The implementation for web date-time picker widgets.
 *
 * @author TG Team
 *
 */
public class DateTimePickerWidget extends AbstractWidget {

    /**
     * Creates an instance of {@link DateTimePickerWidget} for specified entity type and property name.
     *
     * @param entityType
     * @param propertyName
     */
    public DateTimePickerWidget(final Class<? extends AbstractEntity<?>> entityType, final String propertyName) {
        super("editors/tg-datetime-picker", entityType, propertyName);
    }

}
