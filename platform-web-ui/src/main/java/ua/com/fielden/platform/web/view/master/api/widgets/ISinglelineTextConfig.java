package ua.com.fielden.platform.web.view.master.api.widgets;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.helpers.ISkipValidation;
import ua.com.fielden.platform.web.view.master.api.widgets.singlelinetext.ISinglelineTextConfig0;

/**
 *
 * A configuration for a widget to edit string properties that can should be represented as a single line text value. Usually, single line representations are good for short text
 * values. Otherwise, a multiline text widget should be used. Uppercase and pattern related behaviour should be derived automatically from an associated property.
 * <p>
 * In case of HTML this should be <code>input</text> with <code>type="text"</code>.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface ISinglelineTextConfig<T extends AbstractEntity<?>> extends ISinglelineTextConfig0<T>, ISkipValidation<ISinglelineTextConfig0<T>> {
}
