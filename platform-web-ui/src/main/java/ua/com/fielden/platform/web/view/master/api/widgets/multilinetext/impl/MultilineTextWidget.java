package ua.com.fielden.platform.web.view.master.api.widgets.multilinetext.impl;

import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

/**
 * The implementation for web multiline text widgets.
 *
 * @author TG Team
 *
 */
public class MultilineTextWidget extends AbstractWidget {

    /**
     * Creates {@link MultilineTextWidget} from <code>entityType</code> type and <code>propertyName</code>.
     *
     * @param titleDesc
     * @param propertyName
     */
    public MultilineTextWidget(final Pair<String, String> titleDesc, final String propertyName) {
        super("editors/tg-multiline-text-editor", titleDesc, propertyName);
    }

    public MultilineTextWidget resizable() {
        // TODO implement
        // TODO implement
        // TODO implement
        // TODO implement
        // TODO implement

        // TODO must provide an ability to specify whether multiline text widget is resizable or not. Also provide an attribute in the appropriate
        // polymer component that specify whether multiline text widget is resizable or not.
        return this;
    }
}
