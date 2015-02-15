package ua.com.fielden.platform.web.master.api.widgets;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.master.api.widgets.datetimepicker.IDateTimePickerConfig0;

/**
* A configuration for an datetime picker component that gets associated with a property of type <code>Date</code> that resembles both date and time.
*
* As a note, the actual <code>input</code> field could have date and step attributes assigned.
* Such as <code>type="datetime-local"</code> and <code>step=3600</code> for a one hour increment on the associated spinner (tested in Chrome).
*
* @author TG Team
*
* @param <T>
*/
public interface IDateTimePickerConfig<T extends AbstractEntity<?>> extends IDateTimePickerConfig0<T> {
    /**
     * This declaration indicates that an asynchronous validation to a corresponding property should be skipped.
     * This should be done for optimisation reasons only in relation to properties that have heavy validation.
     * It should be understood the actual validation would anyway take place upon entity saving.
     *
     * @param matcher
     * @return
     */
    IDateTimePickerConfig0<T> skipValidation();
}
