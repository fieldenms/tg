package ua.com.fielden.platform.web.view.master.api.widgets.colour.impl;

import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

/**
 * The implementation box for colour text widgets.
 *
 * @author TG Team
 *
 */
public class ColourWidget extends AbstractWidget {
    /**
     * Creates {@link ColourWidget} from <code>entityType</code> type and <code>propertyName</code>.
     *
     * @param titleDesc
     * @param propertyName
     */
    public ColourWidget(final Pair<String, String> titleDesc, final String propertyName) {
        super("editors/tg-colour-picker", titleDesc, propertyName);
    }
}
