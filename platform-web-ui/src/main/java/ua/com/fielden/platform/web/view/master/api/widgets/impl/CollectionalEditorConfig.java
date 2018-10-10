package ua.com.fielden.platform.web.view.master.api.widgets.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.helpers.IPropertySelector;
import ua.com.fielden.platform.web.view.master.api.widgets.ICollectionalEditorConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.collectional.ICollectionalEditorConfig1;
import ua.com.fielden.platform.web.view.master.api.widgets.collectional.ICollectionalEditorConfig2;
import ua.com.fielden.platform.web.view.master.api.widgets.collectional.ICollectionalEditorWithReordering;
import ua.com.fielden.platform.web.view.master.api.widgets.collectional.impl.CollectionalEditorWidget;

public class CollectionalEditorConfig<T extends AbstractEntity<?>>
        extends AbstractEditorWidgetConfig<T, CollectionalEditorWidget, ICollectionalEditorWithReordering<T>>
        implements ICollectionalEditorConfig<T>, ICollectionalEditorWithReordering<T> {

    public CollectionalEditorConfig(final CollectionalEditorWidget widget, final IPropertySelector<T> propSelector) {
        super(widget, propSelector);
    }

    @Override
    public ICollectionalEditorWithReordering<T> skipValidation() {
        skipVal();
        return this;
    }

    @Override
    public ICollectionalEditorConfig2<T> withHeader(final String headerPropertyName) {
        widget().setHeaderPropertyName(headerPropertyName);
        return this;
    }

    @Override
    public ICollectionalEditorConfig2<T> withDescription(final String descriptionPropertyName) {
        widget().setDescriptionPropertyName(descriptionPropertyName);
        return this;
    }

    @Override
    public ICollectionalEditorConfig1<T> reorderable() {
        widget().makeReorderable();
        return this;
    }
}
