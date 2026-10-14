package ua.com.fielden.platform.web.view.master.api.widgets.datetimepicker.impl;

import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

import java.util.Map;

/// The implementation for Web UI date-time picker widgets.
///
public class DateTimePickerWidget extends AbstractWidget {
    private final boolean timePortionToBecomeEndOfDay;

    /// Creates an instance of [DateTimePickerWidget] for specified entity type and property name.
    ///
    public DateTimePickerWidget(final Pair<String, String> titleDesc, final String propertyName, final boolean timePortionToBecomeEndOfDay) {
        super("editors/tg-datetime-picker", titleDesc, propertyName);
        this.timePortionToBecomeEndOfDay = timePortionToBecomeEndOfDay;
    }

    @Override
    protected Map<String, Object> createCustomAttributes() {
        final Map<String, Object> customAttr = super.createCustomAttributes();
        customAttr.put("time-portion-to-become-end-of-day", timePortionToBecomeEndOfDay);
        return customAttr;
    }

}
