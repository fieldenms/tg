package ua.com.fielden.platform.web.view.master.api.widgets.richtext.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

public class RichTextWidget extends AbstractWidget {

    public RichTextWidget(final Pair<String, String> titleDesc, final Class<? extends AbstractEntity<?>> entityType, final String propertyName) {
        super("editors/tg-rich-text-editor", titleDesc, propertyName);
    }
}
