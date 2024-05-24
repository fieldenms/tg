package ua.com.fielden.platform.web.view.master.api.widgets.hyperlink.impl;

import static ua.com.fielden.platform.web.centre.WebApiUtils.webComponent;

import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

/**
 * The implementation for widget to represent values of type {@link Hyperlink}.
 *
 * @author TG Team
 *
 */
public class HyperlinkWidget extends AbstractWidget {

    public HyperlinkWidget(final Pair<String, String> titleDesc, final String propertyName) {
        super(webComponent("editors/tg-hyperlink-editor"), titleDesc, propertyName);
    }
}
