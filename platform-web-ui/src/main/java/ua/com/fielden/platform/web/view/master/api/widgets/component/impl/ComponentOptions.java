package ua.com.fielden.platform.web.view.master.api.widgets.component.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.empty;
import static java.util.Optional.of;

/// The declarations common to both ways of adding an application-provided web component to a master:
/// the element name, the attributes, and whether the component is blocked while the master entity is unsaved.
///
/// Blocking is declared by default.
/// It is rendered as the `block-when-unsaved` flag and the modification state of the master:
/// `entity-modified` for committed changes, and `entity-edited` for edits in progress.
/// The client-side component contract combines these with the persistence of the bound entity to block interaction.
///
final class ComponentOptions {

    private Optional<String> elementName = empty();
    private final Map<String, String> attrs = new LinkedHashMap<>();
    private boolean blockWhenUnsaved = true;

    void withElementName(final String elementName) {
        this.elementName = of(elementName);
    }

    void withAttr(final String name, final String value) {
        attrs.put(name, value);
    }

    void skipBlockingWhenUnsaved() {
        this.blockWhenUnsaved = false;
    }

    /// The declared element name, or `defaultElementName` if none was declared.
    ///
    String elementName(final String defaultElementName) {
        return elementName.orElse(defaultElementName);
    }

    /// Adds the blocking attributes to `elementAttrs`, if blocking is declared.
    ///
    void addBlockingAttributes(final Map<String, Object> elementAttrs) {
        if (blockWhenUnsaved) {
            elementAttrs.put("block-when-unsaved", true);
            elementAttrs.put("entity-modified", "[[_bindingEntityModified]]");
            elementAttrs.put("entity-edited", "[[_editedPropsExist]]");
        }
    }

    /// The declared attributes, in the order of declaration.
    ///
    Map<String, String> attributes() {
        return unmodifiableMap(attrs);
    }

}
