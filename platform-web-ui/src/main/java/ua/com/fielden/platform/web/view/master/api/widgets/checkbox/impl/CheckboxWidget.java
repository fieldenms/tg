package ua.com.fielden.platform.web.view.master.api.widgets.checkbox.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

/**
 * An implementation for web boolean editor.
 *
 * @author TG Team
 *
 */
public class CheckboxWidget extends AbstractWidget {

    /**
     * Creates an instance of {@link CheckboxWidget} for specified entity type and property name.
     *
     * @param entityType
     * @param propertyName
     */
    public CheckboxWidget(final Class<? extends AbstractEntity<?>> entityType, final String propertyName) {
        super("editors/tg-boolean-editor", entityType, propertyName);
    }

}
