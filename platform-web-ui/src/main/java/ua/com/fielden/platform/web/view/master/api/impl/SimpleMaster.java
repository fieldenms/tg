package ua.com.fielden.platform.web.view.master.api.impl;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.actions.entity.IEntityActionConfig0;
import ua.com.fielden.platform.web.view.master.api.helpers.IPropertySelector;
import ua.com.fielden.platform.web.view.master.api.helpers.IWidgetSelector;
import ua.com.fielden.platform.web.view.master.api.helpers.impl.WidgetSelector;
import ua.com.fielden.platform.web.view.master.api.widgets.IDividerConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.IHtmlText;

public class SimpleMaster<T extends AbstractEntity<?>> implements IPropertySelector<T> {

    private final List<IWidgetSelector<T>> widgets = new ArrayList<>();

    public final Class<T> entityType;

    public SimpleMaster(final Class<T> entityType) {
        this.entityType = entityType;
    }

    @Override
    public IEntityActionConfig0<T> addAction(final String name, final Class<? extends AbstractEntity<?>> functionalEntity) {
        // TODO Auto-generated method stub
        return null;
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
}
