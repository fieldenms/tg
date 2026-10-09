package ua.com.fielden.platform.web.view.master.api.widgets.component;

import ua.com.fielden.platform.entity.AbstractEntity;

/// A contract for declaring attributes of an entity-bound component element, and whether the component is blocked while the master entity is unsaved.
///
public interface IEntityComponentConfig2<T extends AbstractEntity<?>> extends IEntityComponentConfig3<T> {

    /// Declares an attribute of the component element.
    ///
    /// The name is in dash-case, as it appears in HTML, such as `centre-uuid`.
    /// The value is either static, such as `compact`, or a pass-through binding expression over the master, such as `[[centreUuid]]`.
    /// Attributes are rendered in the order of their first declaration.
    /// Declaring an attribute again replaces its value, which lets a declaration override an attribute set elsewhere, such as in a shared configuration.
    ///
    IEntityComponentConfig2<T> withAttr(final String name, final CharSequence value);

    /// Declares an attribute of the component element that is bound to the value of a property of the entity of the master.
    ///
    /// The property is a name, which may be dot-notated, such as `location.gisInfo`, or a metamodel reference, such as `Location_.gisInfo()`.
    /// The value follows the entity that the component is bound to, and is passed to the component as is, such as an entity, a number or a collection of entities.
    /// The property must be fetched with the master entity.
    /// The attribute is ordered and replaced as one declared with [#withAttr(String, CharSequence)].
    ///
    IEntityComponentConfig2<T> withPropAttr(final String name, final CharSequence propPath);

    /// Keeps the component interactive while the master entity is unsaved.
    ///
    /// By default, a component in a master for a persistent entity type is blocked while the entity is new or has changes that are not yet saved.
    /// A pane then covers the component with a message to save or cancel the changes, and neither the component nor its actions can be interacted with.
    /// This declaration is intended for components that display values of the master entity itself, which stay consistent with it while it is unsaved.
    ///
    IEntityComponentConfig3<T> skipBlockingWhenUnsaved();

}
