package ua.com.fielden.platform.web.view.master.api.widgets.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.actions.property.IPropertyActionConfig0;
import ua.com.fielden.platform.web.view.master.api.actions.property.impl.PropertyActionConfig;
import ua.com.fielden.platform.web.view.master.api.helpers.IPropertySelector;
import ua.com.fielden.platform.web.view.master.api.widgets.IMultilineTextConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.multilinetext.IMultilineTextConfig0;
import ua.com.fielden.platform.web.view.master.api.widgets.multilinetext.IMultilineTextConfig1;
import ua.com.fielden.platform.web.view.master.api.widgets.multilinetext.impl.MultilineTextWidget;

public class MultilineTextConfig<T extends AbstractEntity<?>> implements IMultilineTextConfig<T>, IMultilineTextConfig0<T>, IMultilineTextConfig1<T> {

    private final IPropertySelector<T> propSelector;
    private final MultilineTextWidget widget;

    public MultilineTextConfig(final MultilineTextWidget widget, final IPropertySelector<T> propSelector) {
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
    public IMultilineTextConfig0<T> skipValidation() {
        this.widget.skipValidation();
        return this;
    }

    @Override
    public IMultilineTextConfig1<T> resizable() {
        this.widget.resizable();
        return this;
    }

}
