package ua.com.fielden.platform.web.view.master.api.widgets.richtext.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

import java.util.Map;

import static java.lang.String.format;

public class RichTextWidget extends AbstractWidget {

    private int height = 100; //default height
    private int minHeight = 100; //default min height

    public RichTextWidget(final Pair<String, String> titleDesc, final Class<? extends AbstractEntity<?>> entityType, final String propertyName) {
        super("editors/tg-rich-text-editor", titleDesc, propertyName);
    }

    public void setHeight (int height) {
        this.height = height;
    }

    public void setMinHeight (int minHeight) {
        this.minHeight = minHeight;
    }

    protected Map<String, Object> createCustomAttributes() {
        final Map<String, Object> customAttr = super.createCustomAttributes();
        customAttr.put("height", format("%spx", this.height));
        customAttr.put("min-height", format("%spx", this.minHeight));
        return customAttr;
    }
}
