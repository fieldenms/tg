package ua.com.fielden.platform.web.view.master.api.widgets.datetimepicker.impl;

import java.util.Map;

import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

/**
 * The implementation for web date-time picker widgets.
 *
 * @author TG Team
 *
 */
public class DateTimePickerWidget extends AbstractWidget {
    private final boolean timePortionToBecomeEndOfDay;
    private final String timeZone;

    /**
     * Creates an instance of {@link DateTimePickerWidget} for specified entity type and property name.
     *
     * @param titleDesc
     * @param propertyName
     */
    public DateTimePickerWidget(final Pair<String, String> titleDesc, final String propertyName, final boolean timePortionToBecomeEndOfDay, final String timeZone) {
        super("editors/tg-datetime-picker", titleDesc, propertyName);
        this.timePortionToBecomeEndOfDay = timePortionToBecomeEndOfDay;
        this.timeZone = timeZone;
    }
    
    @Override
    protected Map<String, Object> createCustomAttributes() {
        final Map<String, Object> customAttr = super.createCustomAttributes();
        customAttr.put("time-portion-to-become-end-of-day", timePortionToBecomeEndOfDay);
        if (timeZone != null) {
            customAttr.put("time-zone", timeZone);
        }
        return customAttr;
    }

}
