package ua.com.fielden.platform.web.view.master.api.widgets;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.widgets.email.IEmailConfig0;

/**
 * A configuration for a widget to edit string properties with a meaning of an email address.
 * At some stage there should be a separate type <code>Email</code> that would be used in place of <code>String</code> for property types that should hold an email.
 * This configuration should support both these property types.
 * <p>
 * In case of HTML a corresponding widget should either be an <code>input</text> with <code>type="email"</code> and appropriate pattern, or a custom component.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IEmailConfig<T extends AbstractEntity<?>> extends IEmailConfig0<T> {
    /**
     * This declaration indicates that an asynchronous validation to a corresponding property should be skipped.
     * This should be done for optimisation reasons only in relation to properties that have heavy validation.
     * It should be understood the actual validation would anyway take place upon entity saving.
     *
     * @param matcher
     * @return
     */
    IEmailConfig0<T> skipValidation();
}
