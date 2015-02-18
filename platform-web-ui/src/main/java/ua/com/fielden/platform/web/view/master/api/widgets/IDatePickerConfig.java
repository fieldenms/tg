package ua.com.fielden.platform.web.view.master.api.widgets;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.widgets.datepicker.IDatePickerConfig0;

/**
* A configuration for an date picker component (without a time portion) that gets associated with a property of type <code>Date</code> that resembles date only.
*
* As a note, the actual <code>input</code> field could have date and step attributes assigned.
* Such as <code>type="datetime-local"</code> and <code>step=86400</code> for a one day increment on the associated spinner (tested in Chrome).
*
* @author TG Team
*
* @param <T>
*/
public interface IDatePickerConfig<T extends AbstractEntity<?>> extends IDatePickerConfig0<T> {
    /**
     * This declaration indicates that an asynchronous validation to a corresponding property should be skipped.
     * This should be done for optimisation reasons only in relation to properties that have heavy validation.
     * It should be understood the actual validation would anyway take place upon entity saving.
     *
     * @param matcher
     * @return
     */
    IDatePickerConfig0<T> skipValidation();
}
