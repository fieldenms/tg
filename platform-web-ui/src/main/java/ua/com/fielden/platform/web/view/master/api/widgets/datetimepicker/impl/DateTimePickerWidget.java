package ua.com.fielden.platform.web.view.master.api.widgets.datetimepicker.impl;

import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

import java.util.Map;

/**
 * The implementation for web date-time picker widgets.
 *
 * @author TG Team
 *
 */
public class DateTimePickerWidget extends AbstractWidget {
    private final boolean timePortionToBecomeEndOfDay;
    private final String timeZone;
    private final String datePortion;

    /**
     * Creates an instance of {@link DateTimePickerWidget} for specified entity type and property name.
     *
     * @param titleDesc
     * @param propertyName
     */
    public DateTimePickerWidget(final Pair<String, String> titleDesc, final String propertyName, final boolean timePortionToBecomeEndOfDay, final String timeZone, final String datePortion) {
        super("editors/tg-datetime-picker", titleDesc, propertyName);
        this.timePortionToBecomeEndOfDay = timePortionToBecomeEndOfDay;
        this.timeZone = timeZone;
        this.datePortion = datePortion;
    }

    @Override
    protected Map<String, Object> createCustomAttributes() {
        final Map<String, Object> customAttr = super.createCustomAttributes();
        customAttr.put("time-portion-to-become-end-of-day", timePortionToBecomeEndOfDay);
        if (timeZone != null) {
            customAttr.put("prop", null); // TODO
        }
        if (datePortion != null) {
            customAttr.put("date-portion", datePortion);
        }
        return customAttr;
    }

}
