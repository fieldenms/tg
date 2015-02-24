package ua.com.fielden.platform.web.view.master.api.widgets.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.interfaces.IRenderable;

/**
 * The base implementation box for generic information for all widgets.
 *
 * The information includes <code>entityType</code> type with <code>propertyName</code> (the other derivatives will be domain-driven <code>title</code>, <code>description</code>
 * etc.).
 *
 * All widget implementations should be based on this one and should be extended by widget-specific configuration data.
 *
 * @author TG Team
 *
 */
public abstract class AbstractWidget implements IRenderable {
    private final Class<? extends AbstractEntity<?>> entityType;
    private final String propertyName;
    private final String title;
    private final String desc;
    private final String widgetName;
    private final String widgetPath;

    /**
     * Creates {@link AbstractWidget} from <code>entityType</code> type and <code>propertyName</code> and the name&path of widget.
     *
     * @param entityType
     * @param propertyName
     */
    public AbstractWidget(final String widgetPath, final Class<? extends AbstractEntity<?>> entityType, final String propertyName) {
        this.widgetName = extractNameFrom(widgetPath);
        this.widgetPath = widgetPath;
        this.entityType = entityType;
        this.propertyName = propertyName;

        final Pair<String, String> titleDesc = TitlesDescsGetter.getTitleAndDesc(propertyName, entityType);
        this.title = titleDesc.getKey();
        this.desc = titleDesc.getValue();
    }

    /**
     * The type of the entity to which this editor will be bound.
     *
     * @return
     */
    protected Class<? extends AbstractEntity<?>> entityType() {
        return entityType;
    }

    /**
     * The name of the property to which this editor will be bound.
     *
     * @return
     */
    protected String propertyName() {
        return propertyName;
    }

    /**
     * The title of the property to which this editor will be bound.
     *
     * @return
     */
    protected String title() {
        return title;
    }

    /**
     * The description of the property to which this editor will be bound.
     *
     * @return
     */
    protected String desc() {
        return desc;
    }

    /**
     * Creates an attributes that will be used for widget component generation (generic attributes).
     *
     * @return
     */
    private Map<String, Object> createAttributes() {
        final LinkedHashMap<String, Object> attrs = new LinkedHashMap<>();
        attrs.put("entity", "{{currBindingEntity}}");
        attrs.put("propertyName", this.propertyName);
        attrs.put("onAcceptedValueChanged", "{{validate}}");
        attrs.put("propTitle", this.title);
        attrs.put("propDesc", this.desc);
        attrs.put("currentState", "{{currentState}}");
        // TODO attrs.put("action", "{{actions['SNATCH_BACK_VEHICLE']}}");
        attrs.put("externalRefreshCycle", "{{refreshCycleMode}}");
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
    };

    @Override
    public final DomElement render() {
        return new DomElement(widgetName).attrs(createAttributes()).attrs(createCustomAttributes());
    }

    /**
     * Extracts widget name from its path.
     *
     * @param path
     * @return
     */
    public static String extractNameFrom(final String path) {
        final int lastSlashInd = path.lastIndexOf('/');
        if (lastSlashInd < 0) {
            return path.substring(0, path.indexOf('.'));
        } else {
            return path.substring(lastSlashInd + 1, path.indexOf('.'));
        }
    }
}
