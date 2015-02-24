package ua.com.fielden.platform.web.view.master.api.widgets.singlelinetext.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
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
     * @param entityType
     * @param propertyName
     */
    public SinglelineTextWidget(final Class<? extends AbstractEntity<?>> entityType, final String propertyName) {
        super("editors/tg-singleline-text-editor.html", entityType, propertyName);
    }
}
