package ua.com.fielden.platform.web.view.master.api.widgets.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.helpers.IPropertySelector;
import ua.com.fielden.platform.web.view.master.api.widgets.ISinglelineTextConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.singlelinetext.ISinglelineTextConfig0;
import ua.com.fielden.platform.web.view.master.api.widgets.singlelinetext.impl.SinglelineTextWidget;

public class SinglelineTextConfig<T extends AbstractEntity<?>>
        extends AbstractEditorWidgetConfig<T, SinglelineTextWidget, ISinglelineTextConfig0<T>>
        implements ISinglelineTextConfig<T>, ISinglelineTextConfig0<T> {

    public SinglelineTextConfig(final SinglelineTextWidget widget, final IPropertySelector<T> propSelector) {
        super(widget, propSelector);
    }

    @Override
    public ISinglelineTextConfig0<T> skipValidation() {
        skipVal();
        return this;
    }
}
