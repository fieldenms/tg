package ua.com.fielden.platform.web.view.master.api.widgets.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.helpers.IPropertySelector;
import ua.com.fielden.platform.web.view.master.api.widgets.ICollectionalRepresentorConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.collectional.ICollectionalRepresentorConfig0;
import ua.com.fielden.platform.web.view.master.api.widgets.collectional.impl.CollectionalRepresentorWidget;

public class CollectionalRepresentorConfig<T extends AbstractEntity<?>>
        extends AbstractEditorWidgetConfig<T, CollectionalRepresentorWidget, ICollectionalRepresentorConfig0<T>>
        implements ICollectionalRepresentorConfig<T>, ICollectionalRepresentorConfig0<T> {

    public CollectionalRepresentorConfig(final CollectionalRepresentorWidget widget, final IPropertySelector<T> propSelector) {
        super(widget, propSelector);
    }

    @Override
    public ICollectionalRepresentorConfig0<T> skipValidation() {
        skipVal();
        return this;
    }
}
