package ua.com.fielden.platform.web.view.master.api.widgets.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.helpers.IPropertySelector;
import ua.com.fielden.platform.web.view.master.api.widgets.ICheckboxConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.checkbox.ICheckboxConfig0;
import ua.com.fielden.platform.web.view.master.api.widgets.checkbox.impl.CheckboxWidget;

public class CheckboxConfig<T extends AbstractEntity<?>>
        extends AbstractEditorWidgetConfig<T, CheckboxWidget, ICheckboxConfig0<T>>
        implements ICheckboxConfig<T>, ICheckboxConfig0<T> {

    public CheckboxConfig(final CheckboxWidget widget, final IPropertySelector<T> propSelector) {
        super(widget, propSelector);
    }

    @Override
    public ICheckboxConfig0<T> skipValidation() {
        skipVal();
        return this;
    }
}
