package ua.com.fielden.platform.web.centre.api.crit.impl;

import ua.com.fielden.platform.web.view.master.api.widgets.singlelinetext.impl.SinglelineTextWidget;

/**
 * An implementation for single criterion editor.
 *
 * @author TG Team
 *
 */
public class CriterionWidget extends AbstractCriterionWidget {

    /**
     * Creates an instance of {@link CriterionWidget} for specified entity type and property name.
     *
     * @param criteriaType
     * @param propertyName
     */
    public CriterionWidget(final Class<?> root, final Class<?> managedType, final String propertyName) {
        super("centre/criterion/tg-criterion", propertyName, new SinglelineTextWidget(
                generateSingleTitleDesc(root, managedType, propertyName),
                generateSingleName(root, managedType, propertyName)
                ));
    }
}
