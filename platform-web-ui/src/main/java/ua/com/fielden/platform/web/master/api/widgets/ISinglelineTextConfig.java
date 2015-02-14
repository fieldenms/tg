package ua.com.fielden.platform.web.master.api.widgets;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.master.api.helpers.IAlso;
import ua.com.fielden.platform.web.master.api.helpers.ILayoutConfig;

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
public interface ISinglelineTextConfig<T extends AbstractEntity<?>> extends IAlso<T> {

}
