package ua.com.fielden.platform.web.view.master.api.widgets.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.helpers.IPropertySelector;
import ua.com.fielden.platform.web.view.master.api.widgets.IDecimalConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.decimal.IDecimalConfig0;
import ua.com.fielden.platform.web.view.master.api.widgets.decimal.impl.DecimalWidget;

public class DecimalConfig<T extends AbstractEntity<?>>
        extends AbstractEditorWidgetConfig<T, DecimalWidget, IDecimalConfig0<T>>
        implements IDecimalConfig<T>, IDecimalConfig0<T> {

    public DecimalConfig(final DecimalWidget widget, final IPropertySelector<T> propSelector) {
        super(widget, propSelector);
    }

    @Override
    public IDecimalConfig0<T> skipValidation() {
        skipVal();
        return this;
    }
}
