package ua.com.fielden.platform.web.view.master.api.widgets.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.actions.property.IPropertyActionConfig0;
import ua.com.fielden.platform.web.view.master.api.actions.property.impl.PropertyActionConfig;
import ua.com.fielden.platform.web.view.master.api.helpers.IPropertySelector;
import ua.com.fielden.platform.web.view.master.api.widgets.ISinglelineTextConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.singlelinetext.ISinglelineTextConfig0;
import ua.com.fielden.platform.web.view.master.api.widgets.singlelinetext.impl.SinglelineTextWidget;

public class SinglelineTextConfig<T extends AbstractEntity<?>> implements ISinglelineTextConfig<T>, ISinglelineTextConfig0<T> {

    private final IPropertySelector<T> propSelector;
    private final SinglelineTextWidget widget;

    public SinglelineTextConfig(final SinglelineTextWidget widget, final IPropertySelector<T> propSelector) {
        this.propSelector = propSelector;
        this.widget = widget;
    }

    @Override
    public IPropertySelector<T> also() {
        return propSelector;
    }

    @Override
    public IPropertyActionConfig0<T> withAction(final String name, final Class<? extends AbstractEntity<?>> functionalEntity) {
        return new PropertyActionConfig<>(widget.initAction(name, functionalEntity), propSelector);
    }

    @Override
    public ISinglelineTextConfig0<T> skipValidation() {
        //this.widget.skipValidation()
        // TODO Auto-generated method stub
        return this;
    }

}
