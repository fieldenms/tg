package ua.com.fielden.platform.web.view.master.api.widgets.spinner.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

/**
 * An implementation for web integer editor.
 *
 * @author TG Team
 *
 */
public class SpinnerWidget extends AbstractWidget {

    /**
     * Creates an instance of {@link SpinnerWidget} for specified entity type and property name.
     *
     * @param entityType
     * @param propertyName
     */
    public SpinnerWidget(final Class<? extends AbstractEntity<?>> entityType, final String propertyName) {
        super("editors/tg-integer-editor", entityType, propertyName);
    }

}