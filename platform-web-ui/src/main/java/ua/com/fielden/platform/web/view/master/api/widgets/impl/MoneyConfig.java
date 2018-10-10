package ua.com.fielden.platform.web.view.master.api.widgets.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.helpers.IPropertySelector;
import ua.com.fielden.platform.web.view.master.api.widgets.IMoneyConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.money.IMoneyConfig0;
import ua.com.fielden.platform.web.view.master.api.widgets.money.impl.MoneyWidget;

public class MoneyConfig<T extends AbstractEntity<?>>
        extends AbstractEditorWidgetConfig<T, MoneyWidget, IMoneyConfig0<T>>
        implements IMoneyConfig<T>, IMoneyConfig0<T> {

    public MoneyConfig(final MoneyWidget widget, final IPropertySelector<T> propSelector) {
        super(widget, propSelector);
    }

    @Override
    public IMoneyConfig0<T> skipValidation() {
        skipVal();
        return this;
    }
}
