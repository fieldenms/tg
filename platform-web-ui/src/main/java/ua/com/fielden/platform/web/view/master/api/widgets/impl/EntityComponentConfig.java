package ua.com.fielden.platform.web.view.master.api.widgets.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.actions.multi.EntityMultiActionConfig;
import ua.com.fielden.platform.web.view.master.api.helpers.IAlso;
import ua.com.fielden.platform.web.view.master.api.helpers.IPropertySelector;
import ua.com.fielden.platform.web.view.master.api.widgets.IEntityComponentConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.component.IEntityComponentConfig0;
import ua.com.fielden.platform.web.view.master.api.widgets.component.IEntityComponentConfig1;
import ua.com.fielden.platform.web.view.master.api.widgets.component.IEntityComponentConfig2;
import ua.com.fielden.platform.web.view.master.api.widgets.component.IEntityComponentConfig3;
import ua.com.fielden.platform.web.view.master.api.widgets.component.impl.EntityComponentWidget;

import java.util.Objects;

/// The configuration of a component bound to the entity of a master, which records all declarations in its [EntityComponentWidget].
///
public class EntityComponentConfig<T extends AbstractEntity<?>> implements IEntityComponentConfig<T> {

    private final EntityComponentWidget widget;
    private final IPropertySelector<T> propSelector;

    public EntityComponentConfig(final EntityComponentWidget widget, final IPropertySelector<T> propSelector) {
        this.widget = widget;
        this.propSelector = propSelector;
    }

    @Override
    public IEntityComponentConfig0<T> withTitle(final String title) {
        widget.withTitle(title);
        return this;
    }

    @Override
    public IEntityComponentConfig1<T> withDesc(final String desc) {
        widget.withDesc(desc);
        return this;
    }

    @Override
    public IEntityComponentConfig2<T> withElementName(final String elementName) {
        widget.withElementName(elementName);
        return this;
    }

    @Override
    public IEntityComponentConfig2<T> withAttr(final String name, final CharSequence value) {
        widget.withAttr(name, Objects.toString(value, null));
        return this;
    }

    @Override
    public IEntityComponentConfig3<T> skipBlockingWhenUnsaved() {
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
