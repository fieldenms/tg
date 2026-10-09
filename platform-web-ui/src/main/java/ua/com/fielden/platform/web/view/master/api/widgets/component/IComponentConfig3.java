package ua.com.fielden.platform.web.view.master.api.widgets.component;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.actions.IPropertyActionConfig;
import ua.com.fielden.platform.web.view.master.api.helpers.IAlso;

/// A contract for declaring a property action of a component, or completing its configuration.
///
public interface IComponentConfig3<T extends AbstractEntity<?>> extends IAlso<T>, IPropertyActionConfig<T> {
}
