package ua.com.fielden.platform.web.view.master.api.widgets.money.impl;

import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

/**
 * An implementation for web money editor.
 *
 * @author TG Team
 *
 */
public class MoneyWidget extends AbstractWidget {

    /**
     * Creates an instance of {@link MoneyWidget} for specified entity type and property name.
     *
     * @param titleDesc
     * @param propertyName
     */
    public MoneyWidget(final Pair<String, String> titleDesc, final String propertyName) {
        super("editors/tg-money-editor", titleDesc, propertyName);
    }

}
