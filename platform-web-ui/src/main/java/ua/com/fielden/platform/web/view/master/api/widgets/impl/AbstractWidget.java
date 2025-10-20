package ua.com.fielden.platform.web.view.master.api.widgets.impl;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.actions.multi.EntityMultiActionConfig;
import ua.com.fielden.platform.web.centre.api.actions.multi.SingleActionSelector;
import ua.com.fielden.platform.web.interfaces.IImportable;
import ua.com.fielden.platform.web.interfaces.IRenderable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.of;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;

/// The base implementation box for generic information for all widgets.
///
/// The information includes `entityType` type with `propertyName` (the other derivatives will be domain-driven `title`, `description`
/// etc.).
///
/// All widget implementations should be based on this one and should be extended by widget-specific configuration data.
///
public abstract class AbstractWidget implements IRenderable, IImportable {
    private final String propertyName;
    private final String title;
    private final String desc;
    private final String widgetName;
    private final String widgetPath;
    private Optional<EntityMultiActionConfig> action = Optional.empty();
    private boolean skipValidation = false;
    private boolean debug = false;

    /// Creates [AbstractWidget] from `entityType` type and `propertyName` and the name&path of widget.
    ///
    public AbstractWidget(final String widgetPath, final Pair<String, String> titleDesc, final String propertyName) {
        this.widgetName = extractNameFrom(widgetPath);
        this.widgetPath = widgetPath;
        this.propertyName = propertyName;

        this.title = titleDesc.getKey();
        this.desc = titleDesc.getValue();
    }

    /// The name of the property to which this editor will be bound.
    ///
    public String propertyName() {
        return propertyName;
    }

    /// The title of the property to which this editor will be bound.
    ///
    protected String title() {
        return title;
    }

    /// The description of the property to which this editor will be bound.
    ///
    protected String desc() {
        return desc;
    }

    /// Creates an attributes that will be used for widget component generation (generic attributes).
    ///
    private Map<String, Object> createAttributes() {
        final LinkedHashMap<String, Object> attrs = new LinkedHashMap<>();
        if (isDebug()) {
            attrs.put("debug", "true");
        }
        attrs.put("id", "editor_4_"+ this.propertyName);
        attrs.put("entity", "{{_currBindingEntity}}");
        attrs.put("original-entity", "{{_originalBindingEntity}}");
        attrs.put("previous-modified-properties-holder", "[[_previousModifiedPropertiesHolder]]");
        attrs.put("property-name", this.propertyName);
        attrs.put("validation-callback", this.skipValidation ? "[[doNotValidate]]" : "[[validate]]");
        attrs.put("prop-title", this.title);
        attrs.put("prop-desc", this.desc);
        attrs.put("current-state", "[[currentState]]");
        attrs.put("toaster", "[[toaster]]");
        attrs.put("property-action-index", "[[_propertyActionIndices." + this.propertyName +"]]");
        return attrs;
    }

    /// Creates an attributes that will be used for widget component generation.
    ///
    /// Please, implement this method in descendants (for concrete widgets) to extend the attributes set by widget-specific attributes.
    ///
    protected Map<String, Object> createCustomAttributes() {
        return new LinkedHashMap<>();
    };

    @Override
    public final DomElement render() {
        return new DomElement(widgetName).attrs(createAttributes()).attrs(createCustomAttributes());
    }

    public void withAction(final EntityActionConfig action) {
        this.action = of(new EntityMultiActionConfig(SingleActionSelector.class, listOf(() -> of(action))));
    }

    public void withMultiAction(final EntityMultiActionConfig action) {
        this.action = of(action);
    }

    public Optional<EntityMultiActionConfig> action() {
        return action;
    }

    /// Extracts widget name from its path.
    ///
    public static String extractNameFrom(final String path) {
        final int lastSlashInd = path.lastIndexOf('/');
        if (lastSlashInd < 0) {
            return path;
        } else {
            return path.substring(lastSlashInd + 1);
        }
    }

    public AbstractWidget skipValidation() {
        this.skipValidation = true;
        return this;
    }

    @Override
    public String importPath() {
        return widgetPath;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(final boolean debug) {
        this.debug = debug;
    }
}
