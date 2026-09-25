package ua.com.fielden.platform.web.view.master.api.widgets.component.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistentEntityType;

/// A widget that represents a property with an application-provided web component in place of a platform editor.
///
/// The component is read-only.
/// It is bound to the fully-fledged entity of the master and to the name of the property, and receives the title and description of the property, and the index of the property action to show.
/// Any other data it needs is declared as attributes, with either static values or pass-through binding expressions, such as `[[centreUuid]]`.
///
/// By default, a component in a master for a persistent entity type is blocked while the entity is unsaved, that is, new or with changes that are not yet saved.
/// For that, the element receives the `block-when-unsaved` flag and the state of the master:
/// `entity-unsaved` for an entity that is new or has committed changes, and `entity-edited` for edits in progress.
/// The client-side component contract combines these to block interaction.
///
/// The element id is `component_4_<property>`, which keeps the component apart from editors, addressed by the entity binder through `editor_4_<property>`.
/// The element name defaults to the last segment of the import path.
///
public class ComponentWidget extends AbstractWidget {

    private Optional<String> elementName = empty();
    private final Map<String, String> attrs = new LinkedHashMap<>();
    private boolean blockWhenUnsaved;

    /// Creates a widget for the component at `importPath`, which is resolved as `/resources/<importPath>.js`.
    ///
    /// The component is blocked while the entity is unsaved if `entityType`, the entity type of the master, is persistent.
    ///
    public ComponentWidget(final Pair<String, String> titleDesc, final Class<? extends AbstractEntity<?>> entityType, final String propertyName, final String importPath) {
        super(importPath, titleDesc, propertyName);
        this.blockWhenUnsaved = isPersistentEntityType(entityType);
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
        if (blockWhenUnsaved) {
            attrs.put("block-when-unsaved", true);
            // The not-persistent part of this flag never holds here, because blocking applies to persistent entity types only.
            attrs.put("entity-unsaved", "[[_bindingEntityNotPersistentOrNotPersistedOrModified]]");
            attrs.put("entity-edited", "[[_editedPropsExist]]");
        }
        return attrs;
    }

    @Override
    protected Map<String, Object> createCustomAttributes() {
        final Map<String, Object> customAttrs = super.createCustomAttributes();
        customAttrs.putAll(attrs);
        return customAttrs;
    }

}
