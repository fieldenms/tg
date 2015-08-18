package ua.com.fielden.platform.web.centre.api.resultset.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.api.crit.impl.AbstractCriterionWidget;
import ua.com.fielden.platform.web.interfaces.IImportable;
import ua.com.fielden.platform.web.interfaces.IRenderable;

/**
 * Represents the summary entry on EGI.
 *
 * @author TG Team
 *
 */
public class SummaryElement implements IRenderable, IImportable {

    private final String propertyName;
    private final String widgetName;
    private final String widgetPath;
    private final int width;
    private final Class<?> propertyType;
    private final Pair<String, String> titleDesc;

    /**
     * Creates {@link SummaryElement} with <code>propertyType</code>, <code>propertyName</code> width, title and description.
     *
     * @param criteriaType
     * @param propertyName
     */
    public SummaryElement(final String propertyName, final int width, final Class<?> propertyType, final Pair<String, String> titleDesc) {
        this.widgetName = AbstractCriterionWidget.extractNameFrom("egi/tg-summary-property");
        this.widgetPath = "egi/tg-summary-property";
        this.propertyName = propertyName;
        this.width = width;
        this.propertyType = propertyType;
        this.titleDesc = titleDesc;
    }

    @Override
    public String importPath() {
        return widgetPath;
    }

    /**
     * Creates an attributes that will be used for widget component generation (generic attributes).
     *
     * @return
     */
    private Map<String, Object> createAttributes() {
        final LinkedHashMap<String, Object> attrs = new LinkedHashMap<>();

        attrs.put("property", propertyName); // TODO the problem appears for "" property => translates to 'property' not 'property=""'
        attrs.put("width", width + "px");
        attrs.put("type", egiRepresentationFor(DynamicEntityClassLoader.getOriginalType(this.propertyType)));
        attrs.put("column-title", this.titleDesc.getKey());
        attrs.put("column-desc", this.titleDesc.getValue());
        return attrs;
    }

    private Object egiRepresentationFor(final Class<?> propertyType) {
        return EntityUtils.isEntityType(propertyType) ? propertyType.getName() : (EntityUtils.isBoolean(propertyType) ? "Boolean" : propertyType.getSimpleName());
    }

    @Override
    public DomElement render() {
        return new DomElement(widgetName).attrs(createAttributes());
    }
}
