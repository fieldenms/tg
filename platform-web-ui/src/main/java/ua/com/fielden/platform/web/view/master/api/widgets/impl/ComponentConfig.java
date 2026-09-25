package ua.com.fielden.platform.web.view.master.api.widgets.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.actions.multi.EntityMultiActionConfig;
import ua.com.fielden.platform.web.view.master.api.helpers.IAlso;
import ua.com.fielden.platform.web.view.master.api.helpers.IPropertySelector;
import ua.com.fielden.platform.web.view.master.api.widgets.IComponentConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.component.IComponentConfig0;
import ua.com.fielden.platform.web.view.master.api.widgets.component.IComponentConfig1;
import ua.com.fielden.platform.web.view.master.api.widgets.component.impl.ComponentWidget;

import java.util.Objects;

/// The configuration of a component that represents a property in place of an editor, which records all declarations in its [ComponentWidget].
///
public class ComponentConfig<T extends AbstractEntity<?>> implements IComponentConfig<T> {

    private final ComponentWidget widget;
    private final IPropertySelector<T> propSelector;

    public ComponentConfig(final ComponentWidget widget, final IPropertySelector<T> propSelector) {
        this.widget = widget;
        this.propSelector = propSelector;
    }

    @Override
    public IComponentConfig0<T> withElementName(final String elementName) {
        widget.withElementName(elementName);
        return this;
    }

    @Override
    public IComponentConfig0<T> withAttr(final String name, final CharSequence value) {
        widget.withAttr(name, Objects.toString(value, null));
        return this;
    }

    @Override
    public IComponentConfig1<T> skipBlockingWhenUnsaved() {
        widget.skipBlockingWhenUnsaved();
        return this;
    }

    @Override
    public IAlso<T> withAction(final EntityActionConfig action) {
        widget.withAction(action);
        return this;
    }

    @Override
    public IAlso<T> withMultiAction(final EntityMultiActionConfig action) {
        widget.withMultiAction(action);
        return this;
    }

    @Override
    public IPropertySelector<T> also() {
        return propSelector;
    }

}
