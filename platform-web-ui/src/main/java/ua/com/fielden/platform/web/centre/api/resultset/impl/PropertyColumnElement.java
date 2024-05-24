package ua.com.fielden.platform.web.centre.api.resultset.impl;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static ua.com.fielden.platform.web.centre.WebApiUtils.webComponent;
import static ua.com.fielden.platform.web.centre.api.impl.DynamicColumn.DYN_COL_DESC;
import static ua.com.fielden.platform.web.centre.api.impl.DynamicColumn.DYN_COL_DISPLAY_PROP;
import static ua.com.fielden.platform.web.centre.api.impl.DynamicColumn.DYN_COL_GROUP_PROP;
import static ua.com.fielden.platform.web.centre.api.impl.DynamicColumn.DYN_COL_GROUP_PROP_VALUE;
import static ua.com.fielden.platform.web.centre.api.impl.DynamicColumn.DYN_COL_GROW_FACTOR;
import static ua.com.fielden.platform.web.centre.api.impl.DynamicColumn.DYN_COL_MIN_WIDTH;
import static ua.com.fielden.platform.web.centre.api.impl.DynamicColumn.DYN_COL_TITLE;
import static ua.com.fielden.platform.web.centre.api.impl.DynamicColumn.DYN_COL_TOOLTIP_PROP;
import static ua.com.fielden.platform.web.centre.api.impl.DynamicColumn.DYN_COL_TYPE;
import static ua.com.fielden.platform.web.centre.api.impl.DynamicColumn.DYN_COL_WIDTH;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.api.crit.impl.AbstractCriterionWidget;
import ua.com.fielden.platform.web.interfaces.IImportable;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

/**
 * The implementation for all result set columns (dom element).
 *
 * @author TG Team
 *
 */
public class PropertyColumnElement implements IRenderable, IImportable {
    //The minimal column width. It is used only when specified width is greater than minimal.
    public static final int MIN_COLUMN_WIDTH = 16;
    public static final int DEFAULT_COLUMN_WIDTH = 80;

    private final String propertyName;
    private final boolean isDynamic;
    private final boolean isSortable;
    private final Optional<String> tooltipProp;
    private final String widgetName;
    private final String widgetPath;
    private final Object propertyType;
    private final Pair<String, String> titleDesc;
    private final List<FunctionalActionElement> actions = new ArrayList<>();
    private final List<SummaryElement> summary;
    private boolean debug = false;
    private final int growFactor;
    private final Optional<AbstractWidget> widget;

    public final int width;
    public final boolean isFlexible;

    /**
     * Creates {@link PropertyColumnElement} from <code>entityType</code> type and <code>propertyName</code> and the name&path of widget.
     *
     * @param criteriaType
     * @param propertyName
     */
    public PropertyColumnElement(final String propertyName, final Optional<AbstractWidget> widget, final boolean isDynamic, final boolean isSortable, final int width, final int growFactor, final boolean isFlexible, final String tooltipProp, final Object propertyType, final Pair<String, String> titleDesc, final List<FunctionalActionElement> actions) {
        this.widgetName = AbstractCriterionWidget.extractNameFrom("egi/tg-property-column");
        this.widgetPath = webComponent("egi/tg-property-column");
        this.propertyName = propertyName;
        this.widget = widget;
        this.isDynamic = isDynamic;
        this.isSortable = isSortable;
        this.tooltipProp = Optional.ofNullable(tooltipProp);
        this.width = width;
        this.growFactor = growFactor;
        this.isFlexible = isFlexible;
        this.propertyType = propertyType;
        this.titleDesc = titleDesc;
        this.actions.addAll(actions);
        this.summary = new ArrayList<>();
    }

    public Optional<DomElement> renderWidget() {
        return widget.map(widget -> widget.render().attr("slot", "egi-editor"));
    }

    public Optional<String> widgetImportPath() {
        return widget.map(widget -> widget.importPath());
    }

    /**
     * Adds the summary to this {@link PropertyColumnElement} instance.
     *
     * @param propertyName
     * @param propertyType
     * @param titleDesc
     */
    public void addSummary(final String propertyName, final Class<?> propertyType, final Pair<String, String> titleDesc) {
        summary.add(new SummaryElement(propertyName, propertyType, titleDesc));
    }

    /**
     * Determines whether this {@link PropertyColumnElement} instance has summary.
     *
     * @return
     */
    public boolean hasSummary() {
        return !summary.isEmpty();
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
        attrs.put("debug", isDebug());
        attrs.put("tooltip-property", tooltipBinding());
        attrs.put("property", propertyNameBinding()); // TODO the problem appears for "" property => translates to 'property' not 'property=""'
        attrs.put("collectional-property", collectionalPropertyNameBinding());
        attrs.put("key-property", keyPropertyBinding());
        attrs.put("value-property", valuePropertyBinding());
        attrs.put("slot", "property-column");
        attrs.put("width", widthBinding());
        attrs.put("min-width", minWidthBinding());
        attrs.put("grow-factor", growFactorBinding());
        attrs.put("type", propertyTypeBinding());
        attrs.put("column-title", titleBinding());
        attrs.put("column-desc", descBinding());
        attrs.put("sortable", isSortable);
        attrs.put("editable", widget.isPresent());
        return attrs;
    }

    private String collectionalPropertyNameBinding() {
        return isDynamic ? this.propertyName() : "";
    }

    private String keyPropertyBinding() {
        return isDynamic ? format("[[item.%s]]", DYN_COL_GROUP_PROP) : "";
    }

    private String descBinding() {
        return isDynamic ? format("[[item.%s]]", DYN_COL_DESC) : this.titleDesc.getValue();
    }

    private String titleBinding() {
        return isDynamic ? format("[[item.%s]]", DYN_COL_TITLE) : this.titleDesc.getKey();
    }

    private Object propertyTypeBinding() {
        return isDynamic ? format("[[item.%s]]", DYN_COL_TYPE) : this.propertyType;
    }

    private String growFactorBinding() {
        return isDynamic ? format("[[item.%s]]", DYN_COL_GROW_FACTOR) : (isFlexible ? String.valueOf(growFactor) : "0");
    }

    private String minWidthBinding() {
        return isDynamic ? format("[[item.%s]]", DYN_COL_MIN_WIDTH) : String.valueOf(MIN_COLUMN_WIDTH > width ? width : MIN_COLUMN_WIDTH);
    }

    private String widthBinding() {
        return isDynamic ? format("[[item.%s]]", DYN_COL_WIDTH) : String.valueOf(width);
    }

    private String valuePropertyBinding() {
        return isDynamic ? format("[[item.%s]]", DYN_COL_DISPLAY_PROP) : "";
    }

    private String propertyNameBinding() {
        return isDynamic ? format("[[item.%s]]", DYN_COL_GROUP_PROP_VALUE) : this.propertyName();
    }

    private String tooltipBinding() {
        return isDynamic ? format("[[item.%s]]", DYN_COL_TOOLTIP_PROP) : this.tooltipProp.orElse("");
    }

    private Map<String, Object> createDynamicColumnsAttributes() {
        final LinkedHashMap<String, Object> attrs = new LinkedHashMap<>();
        attrs.put("is", "dom-repeat");
        attrs.put("items", "[[dynamicColumns." + this.propertyName() + "Columns]]");
        return attrs;
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
    }

    public List<FunctionalActionElement> getActions() {
        return unmodifiableList(actions);
    }

    @Override
    public final DomElement render() {
        return isDynamic ? renderDynamicColumn() : renderColumnElement();

    }

    private DomElement renderDynamicColumn() {
        return new DomElement("template").attrs(createDynamicColumnsAttributes()).add(renderColumnElement());
    }

    private DomElement renderColumnElement () {
        final DomElement columnElement = new DomElement(widgetName).attrs(createAttributes()).attrs(createCustomAttributes());
        for (final FunctionalActionElement actionElement : actions) {
            if (actionElement.getFunctionalActionKind() == FunctionalActionKind.PROP) {
                final DomElement actionDomElement = actionElement.render();
                actionDomElement.attr("slot", "property-action");
                if (isDynamic) {
                    actionDomElement.attr("chosen-property", format("[[item.%s]]", DYN_COL_GROUP_PROP_VALUE));
                }
                columnElement.add(actionDomElement);
            }
        }
        summary.forEach(summary -> columnElement.add(summary.render()));
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