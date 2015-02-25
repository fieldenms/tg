package ua.com.fielden.platform.web.view.master.api.impl;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.ILayout.Orientation;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.layout.FlexLayout;
import ua.com.fielden.platform.web.view.master.api.actions.entity.IEntityActionConfig0;
import ua.com.fielden.platform.web.view.master.api.actions.entity.impl.EntityAction;
import ua.com.fielden.platform.web.view.master.api.actions.entity.impl.EntityActionConfig;
import ua.com.fielden.platform.web.view.master.api.helpers.ILayoutConfig;
import ua.com.fielden.platform.web.view.master.api.helpers.ILayoutConfigWithDone;
import ua.com.fielden.platform.web.view.master.api.helpers.IPropertySelector;
import ua.com.fielden.platform.web.view.master.api.helpers.IWidgetSelector;
import ua.com.fielden.platform.web.view.master.api.helpers.impl.WidgetSelector;
import ua.com.fielden.platform.web.view.master.api.widgets.IDividerConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.IHtmlText;

public class SimpleMaster<T extends AbstractEntity<?>> implements IPropertySelector<T>, ILayoutConfig, ILayoutConfigWithDone {

    private final List<WidgetSelector<T>> widgets = new ArrayList<>();
    private final List<EntityActionConfig<T>> entityActions = new ArrayList<>();
    private final FlexLayout layout = new FlexLayout();

    public final Class<T> entityType;

    public SimpleMaster(final Class<T> entityType) {
        this.entityType = entityType;
    }

    @Override
    public IEntityActionConfig0<T> addAction(final String name, final Class<? extends AbstractEntity<?>> functionalEntity) {
        final EntityActionConfig<T> entityAction = new EntityActionConfig<>(new EntityAction(name, functionalEntity), this);
        entityActions.add(entityAction);
        return entityAction;
    }

    @Override
    public IWidgetSelector<T> addProp(final String propName) {
        final WidgetSelector<T> widget = new WidgetSelector<>(this, propName);
        widgets.add(widget);
        return widget;
    }

    @Override
    public IDividerConfig<T> addDivider() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IHtmlText<T> addHtmlLabel(final String htmlText) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ILayoutConfigWithDone setLayoutFor(final Device device, final Orientation orientation, final String flexString) {
        layout.whenMedia(device, orientation).set(flexString);
        return this;
    }

    @Override
    public IRenderable done() {
        // TODO Auto-generated method stub
        return null;
    }
}
