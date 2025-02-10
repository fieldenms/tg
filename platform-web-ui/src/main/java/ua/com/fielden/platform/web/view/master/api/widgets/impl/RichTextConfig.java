package ua.com.fielden.platform.web.view.master.api.widgets.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.helpers.IPropertySelector;
import ua.com.fielden.platform.web.view.master.api.widgets.IRichTextConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.richtext.IRichTextConfig0;
import ua.com.fielden.platform.web.view.master.api.widgets.richtext.IRichTextConfig1;
import ua.com.fielden.platform.web.view.master.api.widgets.richtext.IRichTextConfigWithMinHeight;
import ua.com.fielden.platform.web.view.master.api.widgets.richtext.impl.RichTextWidget;

/**
 * Implementation for rich text edtir configuration.
 *
 * @param <T>
 *
 * @author TG Team
 */
public class RichTextConfig<T extends AbstractEntity<?>>
        extends AbstractEditorWidgetConfig<T, RichTextWidget, IRichTextConfig0<T>>
        implements IRichTextConfig<T>{

    public RichTextConfig(final RichTextWidget widget, final IPropertySelector<T> propSelector) {
        super(widget, propSelector);
    }

    @Override
    public IRichTextConfig0<T> skipValidation() {
        skipVal();
        return this;
    }

    @Override
    public IRichTextConfigWithMinHeight<T> withHeight(final int height) {
        widget().setHeight(height);
        return this;
    }

    @Override
    public IRichTextConfig1<T> withMinHeight(final int minHeight) {
        widget().setMinHeight(minHeight);
        return this;
    }
}
