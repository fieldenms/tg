package ua.com.fielden.platform.web.centre.api.crit.impl;

import static ua.com.fielden.platform.web.centre.WebApiUtils.webComponent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.widgets.checkbox.impl.CheckboxWidget;

/**
 * An implementation for boolean double-editor criterion.
 *
 * @author TG Team
 *
 */
public class BooleanCriterionWidget extends AbstractCriterionWidget {

    /**
     * Creates an instance of {@link BooleanCriterionWidget} for specified entity type and property name.
     *
     * @param criteriaType
     * @param propertyName
     */
    public BooleanCriterionWidget(final Class<? extends AbstractEntity<?>> root, final Class<?> managedType, final String propertyName) {
        super(root, webComponent("centre/criterion/multi/tg-boolean-criterion"), propertyName,
                new CheckboxWidget(
                        AbstractCriterionWidget.generateTitleDesc(root, managedType, propertyName).getKey(),
                        AbstractCriterionWidget.generateNames(root, managedType, propertyName).getKey()
                ),
                new CheckboxWidget(
                        AbstractCriterionWidget.generateTitleDesc(root, managedType, propertyName).getValue(),
                        AbstractCriterionWidget.generateNames(root, managedType, propertyName).getValue()
                ));
    }

    @Override
    protected String getCriterionClass(final int editorIndex) {
        return "range-criterion-editor-" + (editorIndex + 1);
    }
}
