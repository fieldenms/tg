package ua.com.fielden.platform.web.view.master.api.widgets.component.impl;

import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

import java.util.LinkedHashMap;
import java.util.Map;

/// A widget that represents a property with an application-provided web component in place of a platform editor.
///
/// The component is read-only.
/// It is bound to the fully-fledged entity of the master and to the name of the property, and receives the title and description of the property, and the index of the property action to show.
/// Any other data it needs is declared as attributes, with either static values or pass-through binding expressions, such as `[[centreUuid]]`.
/// By default, the component is blocked while the entity is unsaved, as rendered by [ComponentOptions].
///
/// The element id is `component_4_<property>`, which keeps the component apart from editors, addressed by the entity binder through `editor_4_<property>`.
/// The element name defaults to the last segment of the import path.
///
public class ComponentWidget extends AbstractWidget {

    private final ComponentOptions options = new ComponentOptions();

    /// Creates a widget for the component at `widgetPath`, which is resolved as `/resources/<widgetPath>.js`.
    ///
    public ComponentWidget(final String widgetPath, final Pair<String, String> titleDesc, final String propertyName) {
        super(widgetPath, titleDesc, propertyName);
    }

    /// Overrides the element name that is otherwise derived from the import path.
    ///
    public void withElementName(final String elementName) {
        options.withElementName(elementName);
    }

    /// Declares an attribute of the component element.
    /// Attributes are rendered in the order of declaration.
    ///
    public void withAttr(final String name, final String value) {
        options.withAttr(name, value);
    }

    /// Keeps the component interactive while the entity is unsaved.
    ///
    public void skipBlockingWhenUnsaved() {
        options.skipBlockingWhenUnsaved();
    }

    @Override
    protected String elementName() {
        return options.elementName(super.elementName());
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
        options.addBlockingAttributes(attrs);
        return attrs;
    }

    @Override
    protected Map<String, Object> createCustomAttributes() {
        final Map<String, Object> customAttrs = super.createCustomAttributes();
        customAttrs.putAll(options.attributes());
        return customAttrs;
    }

}
