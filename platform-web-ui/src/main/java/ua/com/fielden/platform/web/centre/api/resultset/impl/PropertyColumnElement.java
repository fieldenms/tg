package ua.com.fielden.platform.web.centre.api.resultset.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
    private final List<SummaryElement> summary;
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
        this.summary = new ArrayList<SummaryElement>();
    }

    /**
     * Adds the summary to this {@link PropertyColumnElement} instance.
     *
     * @param propertyName
     * @param propertyType
     * @param titleDesc
     */
    public void addSummary(final String propertyName, final Class<?> propertyType, final Pair<String, String> titleDesc) {
        summary.add(new SummaryElement(propertyName, width, propertyType, titleDesc));
    }

    /**
     * Determines whether this {@link PropertyColumnElement} instance has totals or not.
     *
     * @return
     */
    public boolean hasSummary() {
        return summary.size() > 0;
    }

    /**
     * Returns the size of summary properties associated with this {@link PropertyColumnElement} instance.
     *
     * @return
     */
    public int getSummaryCount() {
        return summary.size();
    }

    /**
     * Returns the summary property associated with this {@link PropertyColumnElement} instance at specified position.
     *
     * @param index
     * @return
     */
    public SummaryElement getSummary(final int index) {
        return summary.get(index);
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
        attrs.put("type", egiRepresentationFor(DynamicEntityClassLoader.getOriginalType(this.propertyType)));
        attrs.put("column-title", this.titleDesc.getKey());
        attrs.put("column-desc", this.titleDesc.getValue());
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
        if (action.isPresent() && action.get().getFunctionalActionKind() == FunctionalActionKind.PROP) {
            columnElement.add(action.get().render());
        }
        if (hasSummary()) {
            summary.forEach(summary -> columnElement.add(summary.render()));
        }
        return columnElement;
    }

    @Override
    public String importPath() {
        return widgetPath;

        // TODO 'action' needs an import of 'tg-ui-action'!
        // TODO 'action' needs an import of 'tg-ui-action'!
        // TODO 'action' needs an import of 'tg-ui-action'!
        // TODO 'action' needs an import of 'tg-ui-action'!
        // TODO 'action' needs an import of 'tg-ui-action'!
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(final boolean debug) {
        this.debug = debug;
    }
}