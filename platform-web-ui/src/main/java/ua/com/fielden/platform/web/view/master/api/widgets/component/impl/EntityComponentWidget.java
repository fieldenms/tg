package ua.com.fielden.platform.web.view.master.api.widgets.component.impl;

import ua.com.fielden.platform.entity.AbstractEntity;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static ua.com.fielden.platform.utils.Pair.pair;

/// A widget that represents an application-provided web component bound to the entity of a master rather than to any of its properties.
/// The component is placed in the master layout next to the editors.
///
/// The component is read-only.
/// It is bound to the fully-fledged entity of the master, and receives the title and description declared for it, if any, the index of the action to show,
/// and the context of the master.
/// Any other data it needs is declared as attributes, with either static values or pass-through binding expressions, such as `[[centreUuid]]`.
/// By default, the component is blocked while the entity is unsaved, as rendered by [AbstractComponentWidget].
///
/// The component is identified in the master by [#componentKey()], which is both the element id and the key of its action index.
///
/// The widget has no property name, so its actions have no chosen property.
/// Its action is declared as for property widgets: a single action, or a multi-action whose selector chooses the action to show for the entity.
///
public class EntityComponentWidget extends AbstractComponentWidget {

    private final String componentKey;
    private Optional<String> title = empty();
    private Optional<String> desc = empty();

    /// Creates a widget for the component at `importPath`, which is resolved as `/resources/<importPath>.js`, in a master for `entityType`.
    ///
    public EntityComponentWidget(final String importPath, final Class<? extends AbstractEntity<?>> entityType, final String componentKey) {
        super(importPath, pair(null, null), entityType, null);
        this.componentKey = componentKey;
    }

    /// Declares the title of the component, rendered as `prop-title`.
    ///
    public void withTitle(final String title) {
        validateText("title", title);
        this.title = of(title);
    }

    /// Declares the description of the component, rendered as `prop-desc`.
    ///
    public void withDesc(final String desc) {
        validateText("description", desc);
        this.desc = of(desc);
    }

    /// The key that identifies the component in the master, which distinguishes the components of a master from one another, from its editors and from its properties.
    /// It is the id of the component element, and the key under which the index of the action to show is calculated for the entity, among the property action indices of the master.
    ///
    public String componentKey() {
        return componentKey;
    }

    @Override
    protected Map<String, Object> createAttributes() {
        final LinkedHashMap<String, Object> attrs = new LinkedHashMap<>();
        if (isDebug()) {
            attrs.put("debug", "true");
        }
        attrs.put("id", componentKey);
        attrs.put("entity", "[[_currEntity]]");
        title.ifPresent(value -> attrs.put("prop-title", value));
        desc.ifPresent(value -> attrs.put("prop-desc", value));
        attrs.put("property-action-index", "[[_propertyActionIndices." + componentKey + "]]");
        addUnsavedStateAttributes(attrs);
        addMasterContextAttribute(attrs);
        return attrs;
    }

}
