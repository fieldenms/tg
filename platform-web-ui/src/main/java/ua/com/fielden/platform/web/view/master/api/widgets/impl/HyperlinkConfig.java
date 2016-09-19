package ua.com.fielden.platform.web.view.master.api.widgets.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.helpers.IPropertySelector;
import ua.com.fielden.platform.web.view.master.api.widgets.IHyperlinkConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.hyperlink.IHyperlinkConfig0;
import ua.com.fielden.platform.web.view.master.api.widgets.hyperlink.impl.HyperlinkWidget;

public class HyperlinkConfig<T extends AbstractEntity<?>>
        extends AbstractEditorWidgetConfig<T, HyperlinkWidget, IHyperlinkConfig0<T>>
        implements IHyperlinkConfig<T>, IHyperlinkConfig0<T> {

    public HyperlinkConfig(final HyperlinkWidget widget, final IPropertySelector<T> propSelector) {
        super(widget, propSelector);
    }

    @Override
    public IHyperlinkConfig0<T> skipValidation() {
        skipVal();
        return this;
    }

}
