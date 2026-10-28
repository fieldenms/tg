package ua.com.fielden.platform.web.view.master.api.widgets.component.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.Pair;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static ua.com.fielden.platform.utils.CollectionUtil.setOf;

/// A widget that represents a property with an application-provided web component in place of a platform editor.
///
/// The component is read-only.
/// It is bound to the fully-fledged entity of the master and to the name of the property, and receives the title and description of the property, unless others are declared for it,
/// the index of the property action to show, the number of times the master has asked it to refresh, the context of the master, and its height and minimum height, if declared.
/// Any other data it needs is declared as attributes, with either static values or pass-through binding expressions, such as `[[centreUuid]]`.
/// By default, the component is blocked while the entity is unsaved, as rendered by [AbstractComponentWidget].
///
/// Besides the refreshes that the master asks for whenever it retrieves or saves the entity, the component is refreshed by default whenever the value of its property changes,
/// whether the user or a definer changed it, which the client-side component contract determines by comparing the values, as the `refresh-on-property-change` flag tells it.
/// A refresh condition declared with `refreshWhen` takes the place of this default, and the flag is not rendered then.
///
/// The element id is `component_4_<property>`, which keeps the component apart from editors, addressed by the entity binder through `editor_4_<property>`.
///
public class ComponentWidget extends AbstractComponentWidget {

    /// Creates a widget for the component at `widgetPath`, which is resolved as `/resources/<widgetPath>.js`, for `propertyName` of `entityType`.
    ///
    public ComponentWidget(final String widgetPath, final Pair<String, String> titleDesc, final Class<? extends AbstractEntity<?>> entityType, final String propertyName) {
        super(widgetPath, titleDesc, entityType, propertyName);
    }

    @Override
    protected Set<String> additionalReservedAttrNames() {
        return setOf("property-name", "refresh-on-property-change");
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
        attrs.put("property-action-index", propertyActionIndexBinding(propertyName()));
        attrs.put("refresh-count", refreshCountBinding(propertyName()));
        attrs.put("refresh-on-property-change", refreshCondition().isEmpty()); // a `false` boolean attribute is not rendered
        addHeightAttributes(attrs);
        addUnsavedStateAttributes(attrs);
        addMasterContextAttribute(attrs);
        return attrs;
    }

}
