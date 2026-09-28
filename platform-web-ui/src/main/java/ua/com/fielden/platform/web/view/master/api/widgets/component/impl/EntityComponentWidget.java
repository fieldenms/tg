package ua.com.fielden.platform.web.view.master.api.widgets.component.impl;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.interfaces.IImportable;
import ua.com.fielden.platform.web.interfaces.IRenderable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableList;
import static ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget.extractNameFrom;

/// A widget that represents an application-provided web component bound to the entity of a master rather than to any of its properties.
/// The component is placed in the master layout next to the editors.
///
/// The component is read-only.
/// It is bound to the fully-fledged entity of the master.
/// Any other data it needs is declared as attributes, with either static values or pass-through binding expressions, such as `[[centreUuid]]`.
/// By default, the component is blocked while the entity is unsaved, as rendered by [ComponentOptions].
///
/// The element name defaults to the last segment of the import path.
/// Actions of the component are declared here, and are rendered by the master as children of the component element, for the component to invoke.
///
public class EntityComponentWidget implements IRenderable, IImportable {

    private final String importPath;
    private final ComponentOptions options = new ComponentOptions();
    private final List<EntityActionConfig> actions = new ArrayList<>();

    /// Creates a widget for the component at `importPath`, which is resolved as `/resources/<importPath>.js`.
    ///
    public EntityComponentWidget(final String importPath) {
        this.importPath = importPath;
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

    /// Declares an action that the component invokes itself.
    ///
    public void withAction(final EntityActionConfig action) {
        actions.add(action);
    }

    /// The actions of the component, in the order of declaration.
    ///
    public List<EntityActionConfig> actions() {
        return unmodifiableList(actions);
    }

    @Override
    public String importPath() {
        return importPath;
    }

    @Override
    public DomElement render() {
        final Map<String, Object> attrs = new LinkedHashMap<>();
        attrs.put("entity", "[[_currEntity]]");
        options.addBlockingAttributes(attrs);
        attrs.putAll(options.attributes());
        return new DomElement(options.elementName(extractNameFrom(importPath))).attrs(attrs);
    }

}
