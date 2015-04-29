package ua.com.fielden.platform.web.centre.api.resultset.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.api.crit.impl.AbstractCriterionWidget;
import ua.com.fielden.platform.web.interfaces.IImportable;
import ua.com.fielden.platform.web.interfaces.IRenderable;

/**
 * The implementation for all result set columns (dom element).
 *
 * @author TG Team
 *
 */
public class PropertyColumnElement implements IRenderable, IImportable {
    private final String propertyName;
    private final String widgetName;
    private final String widgetPath;
    private final int width;
    private final Class<?> propertyType;
    private final Pair<String, String> titleDesc;
    private final Optional<FunctionalActionElement> action;
    private boolean debug = false;

    /**
     * Creates {@link PropertyColumnElement} from <code>entityType</code> type and <code>propertyName</code> and the name&path of widget.
     *
     * @param criteriaType
     * @param propertyName
     */
    public PropertyColumnElement(final String propertyName, final int width, final Class<?> propertyType, final Pair<String, String> titleDesc, final Optional<FunctionalActionElement> action) {
        this.widgetName = AbstractCriterionWidget.extractNameFrom("egi/tg-property-column");
        this.widgetPath = "egi/tg-property-column";
        this.propertyName = propertyName;
        this.width = width;
        this.propertyType = propertyType;
        this.titleDesc = titleDesc;
        this.action = action;
    }

    /**
     * The name of the property for this column.
     *
     * @return
     */
    protected String propertyName() {
        return propertyName;
    }

    /**
     * Creates an attributes that will be used for widget component generation (generic attributes).
     *
     * @return
     */
    private Map<String, Object> createAttributes() {
        final LinkedHashMap<String, Object> attrs = new LinkedHashMap<>();
        if (isDebug()) {
            attrs.put("debug", "true");
        }
        attrs.put("property", this.propertyName()); // TODO the problem appears for "" property => translates to 'property' not 'property=""'
        attrs.put("width", width + "px");
        if (this.action.isPresent() && this.action.get().getFunctionalActionKind() == FunctionalActionKind.PROP && this.action.get().isMasterInvocationAction()) {
            attrs.put("action", "{{showMaster}}");
        }
        attrs.put("type", egiRepresentationFor(DynamicEntityClassLoader.getOriginalType(this.propertyType)));
        attrs.put("columnTitle", this.titleDesc.getKey());
        attrs.put("columnDesc", this.titleDesc.getValue());
        return attrs;
    }

    private Object egiRepresentationFor(final Class<?> propertyType) {
        return EntityUtils.isEntityType(propertyType) ? propertyType.getName() : (EntityUtils.isBoolean(propertyType) ? "Boolean" : propertyType.getSimpleName());
    }

    /**
     * Creates an attributes that will be used for widget component generation.
     * <p>
     * Please, implement this method in descendants (for concrete widgets) to extend the attributes set by widget-specific attributes.
     *
     * @return
     */
    protected Map<String, Object> createCustomAttributes() {
        return new LinkedHashMap<>();
    };

    public Optional<FunctionalActionElement> getAction() {
        return action;
    }

    @Override
    public final DomElement render() {
        final DomElement columnElement = new DomElement(widgetName).attrs(createAttributes()).attrs(createCustomAttributes());
        if (action.isPresent() && action.get().getFunctionalActionKind() == FunctionalActionKind.PROP && !this.action.get().isMasterInvocationAction()) {
            columnElement.add(action.get().render());
        }
        return columnElement;
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
