package ua.com.fielden.platform.web.master.api.helpers;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.master.api.actions.IEntityActionConfig;
import ua.com.fielden.platform.web.master.api.widgets.IDividerConfig;
import ua.com.fielden.platform.web.master.api.widgets.IHtmlText;

/**
 *
 * Provides a way to add a property of the designated entity type to the master being constructed.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IPropertySelector<T extends AbstractEntity<?>> extends IEntityActionConfig<T> {

    IWidgetSelector<T> addProp(final String propName);

    IDividerConfig<T> addDivider();

    IHtmlText<T> addHtmlLabel(final String htmlText);

}
