package ua.com.fielden.platform.web.view.master.api.widgets;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.helpers.ISkipValidation;
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
public interface IHiddenTextConfig<T extends AbstractEntity<?>> extends IHiddenTextConfig0<T>, ISkipValidation<IHiddenTextConfig0<T>> {
}
