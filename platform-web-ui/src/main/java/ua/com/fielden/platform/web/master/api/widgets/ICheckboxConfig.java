package ua.com.fielden.platform.web.master.api.widgets;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.master.api.widgets.checkbox.ICheckboxConfig0;

/**
*
* A configuration for a widget to edit boolean properties.
* At some stage there should be a separate type <code>Colour</code> that would be used in place of <code>String</code> for property types that should hold a colour.
* This configuration should support both these property types.
* <p>
* In case of HTML a corresponding widget should either be an <code>input</text> with <code>type="checkbox"</code> or a custom component.
*
* @author TG Team
*
* @param <T>
*/
public interface ICheckboxConfig <T extends AbstractEntity<?>> extends ICheckboxConfig0<T> {
    /**
     * This declaration indicates that an asynchronous validation to a corresponding property should be skipped.
     * This should be done for optimisation reasons only in relation to properties that have heavy validation.
     * It should be understood the actual validation would anyway take place upon entity saving.
     *
     * @param matcher
     * @return
     */
    ICheckboxConfig0<T> skipValidation();
}
