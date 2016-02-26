package ua.com.fielden.platform.web.view.master.api.widgets;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.helpers.ISkipValidation;
import ua.com.fielden.platform.web.view.master.api.widgets.phonenumber.IPhoneNumberConfig0;

/**
 *
 * A configuration for a widget to edit string properties with a meaning of a telephone number. At some stage there should be a separate type <code>Phone</code> that would be used
 * in place of <code>String</code> for property types that should hold a telephone number. This configuration should support both these property types.
 * <p>
 * In case of HTML a corresponding widget should either be an <code>input</text> with <code>type="tel"</code> and appropriate pattern, or a custom component.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IPhoneNumberConfig<T extends AbstractEntity<?>> extends IPhoneNumberConfig0<T>, ISkipValidation<IPhoneNumberConfig0<T>> {

}
