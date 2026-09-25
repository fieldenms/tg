package ua.com.fielden.platform.web.view.master.api.widgets.component;

import ua.com.fielden.platform.entity.AbstractEntity;

/// A contract for declaring attributes of a component element, and whether the component is blocked while the master entity is unsaved.
///
public interface IComponentConfig0<T extends AbstractEntity<?>> extends IComponentConfig1<T> {

    /// Declares an attribute of the component element.
    ///
    /// The name is in dash-case, as it appears in HTML, such as `centre-uuid`.
    /// The value is either static, such as `compact`, or a pass-through binding expression over the master, such as `[[centreUuid]]`.
    /// Attributes are rendered in the order of declaration.
    ///
    IComponentConfig0<T> withAttr(final String name, final CharSequence value);

    /// Keeps the component interactive while the master entity is unsaved.
    ///
    /// By default, a component in a master for a persistent entity type is blocked while the entity is new or has changes that are not yet saved.
    /// A pane then covers the component with a message to save or cancel the changes, and neither the component nor its property actions can be interacted with.
    /// This declaration is intended for components that display values of the master entity itself, which stay consistent with it while it is unsaved.
    ///
    IComponentConfig1<T> skipBlockingWhenUnsaved();

}
