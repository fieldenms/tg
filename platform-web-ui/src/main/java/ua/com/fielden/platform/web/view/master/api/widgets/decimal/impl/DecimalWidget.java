package ua.com.fielden.platform.web.view.master.api.widgets.decimal.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

/**
 * An implementation for web decimal editor.
 *
 * @author TG Team
 *
 */
public class DecimalWidget extends AbstractWidget {

    /**
     * Creates an instance of {@link DecimalWidget} for specified entity type and property name.
     *
     * @param entityType
     * @param propertyName
     */
    public DecimalWidget(final Class<? extends AbstractEntity<?>> entityType, final String propertyName) {
        super("editors/tg-decimal-editor", entityType, propertyName);
    }

}
