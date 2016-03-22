package ua.com.fielden.platform.web.view.master.api.widgets.spinner.impl;

import ua.com.fielden.platform.utils.Pair;
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
     * @param titleDesc
     * @param propertyName
     */
    public SpinnerWidget(final Pair<String, String> titleDesc, final String propertyName) {
        super("editors/tg-integer-editor", titleDesc, propertyName);
    }

}