package ua.com.fielden.platform.web.view.master.api.widgets.component;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.actions.IPropertyActionConfig;
import ua.com.fielden.platform.web.view.master.api.helpers.IAlso;

/// A contract for declaring an action of an entity-bound component, or completing its configuration.
///
/// The action is declared as for editors and property components: a single action, or a multi-action whose selector chooses the action to show for the entity.
///
public interface IEntityComponentConfig3<T extends AbstractEntity<?>> extends IAlso<T>, IPropertyActionConfig<T> {
}
