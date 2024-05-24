package ua.com.fielden.platform.web.view.master.api.widgets.decimal.impl;

import static ua.com.fielden.platform.web.centre.WebApiUtils.webComponent;

import ua.com.fielden.platform.utils.Pair;
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
     * @param titleDesc
     * @param propertyName
     */
    public DecimalWidget(final Pair<String, String> titleDesc, final String propertyName) {
        super(webComponent("editors/tg-decimal-editor"), titleDesc, propertyName);
    }

}
