package ua.com.fielden.platform.web.view.master.api.widgets;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.helpers.ISkipValidation;
import ua.com.fielden.platform.web.view.master.api.widgets.collectional.ICollectionalRepresentorConfig0;

/**
 *
 * A configuration for a widget to review collectional properties (entities) that can should be represented as a single line text value.
 * <p>
 * In case of HTML this should be <code>input</text> with <code>type="text"</code>.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface ICollectionalRepresentorConfig<T extends AbstractEntity<?>> extends ICollectionalRepresentorConfig0<T>, ISkipValidation<ICollectionalRepresentorConfig0<T>> {
}
