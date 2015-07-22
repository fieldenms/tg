package ua.com.fielden.platform.web.centre.api.crit.impl;

import java.util.Map;

import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

/**
 * A base implementation for multi criterion (not crit-only single).
 *
 * @author TG Team
 *
 */
public abstract class AbstractMultiCriterionWidget extends AbstractCriterionWidget {

    /**
     * Creates an instance of {@link AbstractMultiCriterionWidget} for specified entity type and property name.
     *
     * @param criteriaType
     * @param propertyName
     */
    public AbstractMultiCriterionWidget(final String widgetPath, final String propertyName, final AbstractWidget... editors) {
        super(widgetPath, propertyName, editors);
    }

    @Override
    protected Map<String, Object> createCustomAttributes() {
        final Map<String, Object> attrs = super.createCustomAttributes();
        // TODO: need to deal with '' -- empty property name, which is used for 'entity itself property'!
        attrs.put("or-null", "{{propertyModel." + this.propertyName() + ".orNull}}");
        attrs.put("not", "{{propertyModel." + this.propertyName() + ".not}}");
        return attrs;
    }
}
