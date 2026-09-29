package ua.com.fielden.platform.web.view.master.api.widgets.component.impl;

import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

/// The base widget for both ways of adding an application-provided web component to a master:
/// in place of an editor, with [ComponentWidget], or bound to the entity rather than to a property, with [EntityComponentWidget].
///
/// It records the element name, the attributes, and whether the component is blocked while the master entity is unsaved.
/// The element name defaults to the last segment of the import path.
/// The declared attributes are rendered after the attributes of a concrete widget, in the order of declaration.
///
/// Blocking is declared by default, and is rendered as the `block-when-unsaved` flag.
/// The modification state of the master is rendered regardless of the flag, so that it is available to the component also when blocking changes at run time:
/// `entity-modified` for committed changes, and `entity-edited` for edits in progress.
/// The client-side component contract combines these with the persistence of the bound entity to block interaction.
///
public abstract class AbstractComponentWidget extends AbstractWidget {

    private Optional<String> elementName = empty();
    private final Map<String, String> attrs = new LinkedHashMap<>();
    private boolean blockWhenUnsaved = true;

    /// Creates a widget for the component at `importPath`, which is resolved as `/resources/<importPath>.js`.
    ///
    protected AbstractComponentWidget(final String importPath, final Pair<String, String> titleDesc, final String propertyName) {
        super(importPath, titleDesc, propertyName);
    }

    /// Overrides the element name that is otherwise derived from the import path.
    ///
    public void withElementName(final String elementName) {
        this.elementName = of(elementName);
    }

    /// Declares an attribute of the component element.
    /// Attributes are rendered in the order of declaration.
    ///
    public void withAttr(final String name, final String value) {
        attrs.put(name, value);
    }

    /// Keeps the component interactive while the entity is unsaved.
    ///
    public void skipBlockingWhenUnsaved() {
        this.blockWhenUnsaved = false;
    }

    @Override
    protected String elementName() {
        return elementName.orElseGet(super::elementName);
    }

    /// Adds the attributes for the unsaved state of the master entity to `elementAttrs`:
    /// the `block-when-unsaved` flag, which is rendered only if blocking is declared, and the modification state of the master.
    ///
    protected void addUnsavedStateAttributes(final Map<String, Object> elementAttrs) {
        elementAttrs.put("block-when-unsaved", blockWhenUnsaved); // a `false` boolean attribute is not rendered
        elementAttrs.put("entity-modified", "[[_bindingEntityModified]]");
        elementAttrs.put("entity-edited", "[[_editedPropsExist]]");
    }

    @Override
    protected Map<String, Object> createCustomAttributes() {
        final Map<String, Object> customAttrs = super.createCustomAttributes();
        customAttrs.putAll(attrs);
        return customAttrs;
    }

}
