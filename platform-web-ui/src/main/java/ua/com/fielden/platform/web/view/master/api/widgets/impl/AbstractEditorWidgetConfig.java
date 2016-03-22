package ua.com.fielden.platform.web.view.master.api.widgets.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.view.master.api.actions.IPropertyActionConfig;
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
    public IAlso<T> withAction(final EntityActionConfig action) {
        widget.withAction(action);
        return this;
    }

    @Override
    public IPropertySelector<T> also() {
        return propSelector;
    }

    public WIDGET widget() {
        return widget;
    }
}
