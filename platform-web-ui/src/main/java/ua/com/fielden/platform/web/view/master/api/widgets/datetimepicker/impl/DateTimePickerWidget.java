package ua.com.fielden.platform.web.view.master.api.widgets.datetimepicker.impl;

import ua.com.fielden.platform.utils.Pair;
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
     * @param titleDesc
     * @param propertyName
     */
    public DateTimePickerWidget(final Pair<String, String> titleDesc, final String propertyName) {
        super("editors/tg-datetime-picker", titleDesc, propertyName);
    }

}
