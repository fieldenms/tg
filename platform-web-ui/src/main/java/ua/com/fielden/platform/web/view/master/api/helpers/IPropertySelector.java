package ua.com.fielden.platform.web.view.master.api.helpers;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.actions.IEntityActionConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.IDividerConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.IEntityComponentConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.IHtmlTextConfig;

/**
 *
 * Provides a way to add a property of the designated entity type to the master being constructed.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IPropertySelector<T extends AbstractEntity<?>> extends IEntityActionConfig<T> {

    IWidgetSelector<T> addProp(final CharSequence propName);

    IDividerConfig<T> addDivider();

    IHtmlTextConfig<T> addHtmlLabel(final String htmlText);

    /// Adds an application-provided web component that is bound to the entity of the master rather than to any of its properties.
    ///
    /// The component is read-only and is placed in the master layout next to the editors, in the order of declaration.
    /// `importPath` is resolved as `/resources/<importPath>.js`, such as `components/tg-vehicle-fuel-usages`.
    ///
    IEntityComponentConfig<T> addComponent(final CharSequence importPath);

}
