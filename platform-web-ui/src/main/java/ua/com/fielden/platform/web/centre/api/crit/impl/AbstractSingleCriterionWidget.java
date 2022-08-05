package ua.com.fielden.platform.web.centre.api.crit.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

/**
 * A base implementation for crit-only single editor criterion.
 *
 * @author TG Team
 *
 */
public abstract class AbstractSingleCriterionWidget extends AbstractCriterionWidget {

    /**
     * Creates an instance of {@link AbstractSingleCriterionWidget} for specified entity type and property name.
     *
     * @param criteriaType
     * @param propertyName
     */
    public AbstractSingleCriterionWidget(final Class<? extends AbstractEntity<?>> root, final String propertyName, final AbstractWidget... editors) {
        super(root, "centre/criterion/tg-criterion", propertyName, editors);
    }
}
