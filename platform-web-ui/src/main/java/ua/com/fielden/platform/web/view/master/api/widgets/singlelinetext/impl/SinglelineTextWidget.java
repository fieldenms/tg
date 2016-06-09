package ua.com.fielden.platform.web.view.master.api.widgets.singlelinetext.impl;

import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

/**
 * The implementation box for singleline text widgets.
 *
 * @author TG Team
 *
 */
public class SinglelineTextWidget extends AbstractWidget {
    /**
     * Creates {@link SinglelineTextWidget} from <code>entityType</code> type and <code>propertyName</code>.
     *
     * @param titleDesc
     * @param propertyName
     */
    public SinglelineTextWidget(final Pair<String, String> titleDesc, final String propertyName) {
        super("editors/tg-singleline-text-editor", titleDesc, propertyName);
    }
}
