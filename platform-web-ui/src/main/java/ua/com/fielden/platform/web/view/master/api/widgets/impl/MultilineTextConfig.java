package ua.com.fielden.platform.web.view.master.api.widgets.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.helpers.IPropertySelector;
import ua.com.fielden.platform.web.view.master.api.widgets.IMultilineTextConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.multilinetext.IMultilineTextConfig0;
import ua.com.fielden.platform.web.view.master.api.widgets.multilinetext.IMultilineTextConfig1;
import ua.com.fielden.platform.web.view.master.api.widgets.multilinetext.IMultilineTextConfigWithRowNumber;
import ua.com.fielden.platform.web.view.master.api.widgets.multilinetext.impl.MultilineTextWidget;

public class MultilineTextConfig<T extends AbstractEntity<?>>
        extends AbstractEditorWidgetConfig<T, MultilineTextWidget, IMultilineTextConfig0<T>>
        implements IMultilineTextConfig<T>, IMultilineTextConfig0<T> {

    public MultilineTextConfig(final MultilineTextWidget widget, final IPropertySelector<T> propSelector) {
        super(widget, propSelector);
    }

    @Override
    public IMultilineTextConfig0<T> skipValidation() {
        skipVal();
        return this;
    }

    @Override
    public IMultilineTextConfigWithRowNumber<T> resizable() {
        this.widget().resizable();
        return this;
    }

    @Override
    public IMultilineTextConfig1<T> withMaxVisibleRows(final int maxRows) {
        this.widget().setMaxVisibleRows(maxRows);
        return this;
    }

}
