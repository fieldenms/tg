package ua.com.fielden.platform.web.view.master.api.widgets.richtext.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

import java.util.Map;

import static java.lang.String.format;

/**
 * The implementation box for rich text widgets.
 *
 * @author TG Team
 *
 */
public class RichTextWidget extends AbstractWidget {

    private final Class<? extends AbstractEntity<?>> entityType;

    private int height = 0; //default height
    private int minHeight = 0; //default min height

    /**
     * Creates new rich text widget for specified property in entityType and specified title/description pair.
     *
     * @param titleDesc
     * @param entityType
     * @param propertyName
     */
    public RichTextWidget(final Pair<String, String> titleDesc, final Class<? extends AbstractEntity<?>> entityType, final String propertyName) {
        super("editors/tg-rich-text-editor", titleDesc, propertyName);
        this.entityType = entityType;
    }

    /**
     * Sets the height of rich text widget
     *
     * @param height
     */
    public void setHeight (int height) {
        this.height = height;
    }

    /**
     * Sets the minimal height of rich text widget
     *
     * @param minHeight
     */
    public void setMinHeight (int minHeight) {
        this.minHeight = minHeight;
    }

    protected Map<String, Object> createCustomAttributes() {
        final Map<String, Object> customAttr = super.createCustomAttributes();
        customAttr.put("entity-type", entityType.getName());
        if (this.height > 0) {
            customAttr.put("height", format("%spx", this.height));
        }
        if (this.minHeight > 0) {
            customAttr.put("min-height", format("%spx", this.minHeight));
        }
        return customAttr;
    }
}
