package ua.com.fielden.platform.web.centre.api.crit.impl;

import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

/**
 * A base implementation for range double-editor criterion.
 *
 * @author TG Team
 *
 */
public abstract class AbstractRangeCriterionWidget extends AbstractCriterionWidget {

    /**
     * Creates an instance of {@link AbstractRangeCriterionWidget} for specified entity type and property name.
     *
     * @param criteriaType
     * @param propertyName
     */
    public AbstractRangeCriterionWidget(final Class<? extends AbstractEntity<?>> root, final String widgetPath, final String propertyName, final AbstractWidget... editors) {
        super(root, widgetPath, propertyName, editors);
    }

    @Override
    protected String getCriterionClass(final int editorIndex) {
        return "range-criterion-editor-" + (editorIndex + 1);
    }

    @Override
    protected Map<String, Object> createCustomAttributes() {
        final Map<String, Object> attrs = super.createCustomAttributes();
        attrs.put("exclusive", "{{propertyModel." + this.propertyName() + ".exclusive}}");
        attrs.put("exclusive2", "{{propertyModel." + this.propertyName() + ".exclusive2}}");
        return attrs;
    }
}
