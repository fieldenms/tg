package ua.com.fielden.platform.web.view.master.api.widgets.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.helpers.IPropertySelector;
import ua.com.fielden.platform.web.view.master.api.widgets.IRichTextConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.richtext.IRichTextConfig0;
import ua.com.fielden.platform.web.view.master.api.widgets.richtext.impl.RichTextWidget;

public class RichTextConfig<T extends AbstractEntity<?>>
        extends AbstractEditorWidgetConfig<T, RichTextWidget, IRichTextConfig0<T>>
        implements IRichTextConfig<T>, IRichTextConfig0<T> {

    public RichTextConfig(final RichTextWidget widget, final IPropertySelector<T> propSelector) {
        super(widget, propSelector);
    }

    @Override
    public IRichTextConfig0<T> skipValidation() {
        skipVal();
        return this;
    }
}
