package ua.com.fielden.platform.web.centre.api.crit.impl;

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
}
