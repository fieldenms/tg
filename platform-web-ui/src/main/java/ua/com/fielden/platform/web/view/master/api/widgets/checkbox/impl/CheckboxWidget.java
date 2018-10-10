package ua.com.fielden.platform.web.view.master.api.widgets.checkbox.impl;

import ua.com.fielden.platform.utils.Pair;
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
     * @param titleDesc
     * @param propertyName
     */
    public CheckboxWidget(final Pair<String, String> titleDesc, final String propertyName) {
        super("editors/tg-boolean-editor", titleDesc, propertyName);
    }

}
