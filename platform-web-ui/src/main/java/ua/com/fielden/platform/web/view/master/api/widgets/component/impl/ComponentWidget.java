package ua.com.fielden.platform.web.view.master.api.widgets.component.impl;

import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

/// A widget that represents a property with an application-provided web component in place of a platform editor.
///
/// The component is read-only.
/// It is bound to the fully-fledged entity of the master and to the name of the property, and receives the title and description of the property, and the index of the property action to show.
/// Any other data it needs is declared as attributes, with either static values or pass-through binding expressions, such as `[[centreUuid]]`.
///
/// The element id is `component_4_<property>`, which keeps the component apart from editors, addressed by the entity binder through `editor_4_<property>`.
/// The element name defaults to the last segment of the import path.
///
public class ComponentWidget extends AbstractWidget {

    private Optional<String> elementName = empty();
    private final Map<String, String> attrs = new LinkedHashMap<>();

    /// Creates a widget for the component at `importPath`, which is resolved as `/resources/<importPath>.js`.
    ///
    public ComponentWidget(final Pair<String, String> titleDesc, final String propertyName, final String importPath) {
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

    @Override
    protected String elementName() {
        return elementName.orElseGet(super::elementName);
    }

    @Override
    protected Map<String, Object> createAttributes() {
        final LinkedHashMap<String, Object> attrs = new LinkedHashMap<>();
        if (isDebug()) {
            attrs.put("debug", "true");
        }
        attrs.put("id", "component_4_" + propertyName());
        attrs.put("entity", "[[_currEntity]]");
        attrs.put("property-name", propertyName());
        attrs.put("prop-title", title());
        attrs.put("prop-desc", desc());
        attrs.put("property-action-index", "[[_propertyActionIndices." + propertyName() + "]]");
        return attrs;
    }

    @Override
    protected Map<String, Object> createCustomAttributes() {
        final Map<String, Object> customAttrs = super.createCustomAttributes();
        customAttrs.putAll(attrs);
        return customAttrs;
    }

}
