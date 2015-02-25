package ua.com.fielden.platform.web.view.master.api.widgets.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.actions.IPropertyActionConfig;
import ua.com.fielden.platform.web.view.master.api.actions.property.IPropertyActionConfig0;
import ua.com.fielden.platform.web.view.master.api.actions.property.impl.PropertyActionConfig;
import ua.com.fielden.platform.web.view.master.api.helpers.IAlso;
import ua.com.fielden.platform.web.view.master.api.helpers.IPropertySelector;
import ua.com.fielden.platform.web.view.master.api.helpers.ISkipValidation;

public abstract class AbstractEditorWidgetConfig<T extends AbstractEntity<?>, WIDGET extends AbstractWidget, NEXT_CONFIG> implements IAlso<T>, IPropertyActionConfig<T>, ISkipValidation<NEXT_CONFIG> {
    private final IPropertySelector<T> propSelector;
    private final WIDGET widget;

    public AbstractEditorWidgetConfig(final WIDGET widget, final IPropertySelector<T> propSelector) {
        this.propSelector = propSelector;
        this.widget = widget;
    }

    protected void skipVal() {
        this.widget.skipValidation();
    }

    @Override
    public IPropertyActionConfig0<T> withAction(final String name, final Class<? extends AbstractEntity<?>> functionalEntity) {
        return new PropertyActionConfig<>(widget.initAction(name, functionalEntity), propSelector);
    }

    @Override
    public IPropertySelector<T> also() {
        return propSelector;
    }

    public WIDGET widget() {
        return widget;
    }
}
