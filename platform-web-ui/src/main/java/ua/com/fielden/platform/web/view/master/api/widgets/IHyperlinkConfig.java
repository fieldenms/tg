package ua.com.fielden.platform.web.view.master.api.widgets;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.web.view.master.api.helpers.ISkipValidation;
import ua.com.fielden.platform.web.view.master.api.widgets.hyperlink.IHyperlinkConfig0;

/**
 *
 * A configuration for a widget to edit properties of type {@link Hyperlink}.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IHyperlinkConfig<T extends AbstractEntity<?>> extends IHyperlinkConfig0<T>, ISkipValidation<IHyperlinkConfig0<T>> {
}
