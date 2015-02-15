package ua.com.fielden.platform.web.master.api.widgets;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.master.api.widgets.singlelinetext.ISinglelineTextConfig0;

/**
 *
 * A configuration for a widget to edit string properties that can should be represented as a single line text value.
 * Usually, single line representations are good for short text values. Otherwise, a multiline text widget should be used.
 * Uppercase and pattern related behaviour should be derived automatically from an associated property.
 * <p>
 * In case of HTML this should be <code>input</text> with <code>type="text"</code>.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface ISinglelineTextConfig<T extends AbstractEntity<?>> extends ISinglelineTextConfig0<T> {
    /**
     * This declaration indicates that an asynchronous validation to a corresponding property should be skipped.
     * This should be done for optimisation reasons only in relation to properties that have heavy validation.
     * It should be understood the actual validation would anyway take place upon entity saving.
     *
     * @param matcher
     * @return
     */
    ISinglelineTextConfig0<T> skipValidation();
}
