package ua.com.fielden.platform.web.view.master.api.widgets;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.widgets.hidden.IHiddenTextConfig0;

/**
 *
 * A configuration for a widget to enter passwords or any other string values that should be hidden.
 * <p>
 * In case of HTML this should be <code>input</text> with <code>type="password"</code>.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IHiddenTextConfig<T extends AbstractEntity<?>> extends IHiddenTextConfig0<T> {
    /**
     * This declaration indicates that an asynchronous validation to a corresponding property should be skipped.
     * This should be done for optimisation reasons only in relation to properties that have heavy validation.
     * It should be understood the actual validation would anyway take place upon entity saving.
     *
     * @param matcher
     * @return
     */
    IHiddenTextConfig0<T> skipValidation();
}
