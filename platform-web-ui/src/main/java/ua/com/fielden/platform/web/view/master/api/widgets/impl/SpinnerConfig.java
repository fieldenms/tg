package ua.com.fielden.platform.web.view.master.api.widgets.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.helpers.IPropertySelector;
import ua.com.fielden.platform.web.view.master.api.widgets.ISpinnerConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.spinner.ISpinnerConfig0;
import ua.com.fielden.platform.web.view.master.api.widgets.spinner.impl.SpinnerWidget;

public class SpinnerConfig<T extends AbstractEntity<?>>
        extends AbstractEditorWidgetConfig<T, SpinnerWidget, ISpinnerConfig0<T>>
        implements ISpinnerConfig<T>, ISpinnerConfig0<T> {

    public SpinnerConfig(final SpinnerWidget widget, final IPropertySelector<T> propSelector) {
        super(widget, propSelector);
    }

    @Override
    public ISpinnerConfig0<T> skipValidation() {
        skipVal();
        return this;
    }
}
