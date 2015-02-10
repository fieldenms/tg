package ua.com.fielden.platform.web.master.api.widgets;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.master.api.helpers.IAlso;
import ua.com.fielden.platform.web.master.api.helpers.ILayoutConfig;

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
public interface IHiddenTextConfig<T extends AbstractEntity<?>> extends IAlso<T>, ILayoutConfig {

}
