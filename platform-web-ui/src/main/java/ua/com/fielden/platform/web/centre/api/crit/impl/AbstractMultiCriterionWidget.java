package ua.com.fielden.platform.web.centre.api.crit.impl;

import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
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
    public AbstractMultiCriterionWidget(final Class<? extends AbstractEntity<?>> root, final String widgetPath, final String propertyName, final AbstractWidget... editors) {
        super(root, widgetPath, propertyName, editors);
    }

    @Override
    protected Map<String, Object> createCustomAttributes() {
        final Map<String, Object> attrs = super.createCustomAttributes();
        attrs.put("or-null", "{{propertyModel." + this.propertyName() + ".orNull}}");
        attrs.put("not", "{{propertyModel." + this.propertyName() + ".not}}");
        attrs.put("or-group", "{{propertyModel." + this.propertyName() + ".orGroup}}");
        return attrs;
    }
}
