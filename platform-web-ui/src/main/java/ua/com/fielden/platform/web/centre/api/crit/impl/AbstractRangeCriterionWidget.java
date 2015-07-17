package ua.com.fielden.platform.web.centre.api.crit.impl;

import java.util.Map;

import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

/**
 * A base implementation for range double-editor criterion.
 *
 * @author TG Team
 *
 */
public abstract class AbstractRangeCriterionWidget extends AbstractMultiCriterionWidget {

    /**
     * Creates an instance of {@link AbstractRangeCriterionWidget} for specified entity type and property name.
     *
     * @param criteriaType
     * @param propertyName
     */
    public AbstractRangeCriterionWidget(final String widgetPath, final String propertyName, final AbstractWidget... editors) {
        super(widgetPath, propertyName, editors);
    }

    @Override
    protected Map<String, Object> createCustomAttributes() {
        final Map<String, Object> attrs = super.createCustomAttributes();
        attrs.put("exclusive", "[[propertyModel." + this.propertyName() + ".exclusive]]");
        attrs.put("exclusive2", "[[propertyModel." + this.propertyName() + ".exclusive2]]");
        return attrs;
    }
}
