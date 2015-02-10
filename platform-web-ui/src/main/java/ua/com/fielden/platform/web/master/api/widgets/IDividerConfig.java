package ua.com.fielden.platform.web.master.api.widgets;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.master.api.widgets.divider.IDividerConfig0;

/**
* A configuration for a horizontal divider that should be used to separate logical sections of a single continuous view.
*
* @author TG Team
*
* @param <T>
*/
public interface IDividerConfig<T extends AbstractEntity<?>> extends IDividerConfig0<T> {
    IDividerConfig0<T> withTitle(final String title);
}
