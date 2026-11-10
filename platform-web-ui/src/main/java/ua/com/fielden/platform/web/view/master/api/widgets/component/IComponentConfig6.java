package ua.com.fielden.platform.web.view.master.api.widgets.component;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.actions.IPropertyActionConfig;
import ua.com.fielden.platform.web.view.master.api.helpers.IAlso;

/// A contract for declaring the action of a component, or completing its configuration.
///
/// The action is declared as for editors: a single action, or a multi-action whose selector chooses the action to show for the entity.
/// The action of a component in place of an editor is a property action, whose chosen property is the property of the component.
/// The action of a component bound to the entity is invoked by the component itself, and has no chosen property.
///
public interface IComponentConfig6<T extends AbstractEntity<?>> extends IAlso<T>, IPropertyActionConfig<T> {
}
