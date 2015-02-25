package ua.com.fielden.platform.web.view.master.api.widgets.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.helpers.IPropertySelector;
import ua.com.fielden.platform.web.view.master.api.widgets.IDateTimePickerConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.datetimepicker.IDateTimePickerConfig0;
import ua.com.fielden.platform.web.view.master.api.widgets.datetimepicker.impl.DateTimePickerWidget;

public class DateTimePickerConfig<T extends AbstractEntity<?>>
        extends AbstractEditorWidgetConfig<T, DateTimePickerWidget, IDateTimePickerConfig0<T>>
        implements IDateTimePickerConfig<T>, IDateTimePickerConfig0<T> {

    public DateTimePickerConfig(final DateTimePickerWidget widget, final IPropertySelector<T> propSelector) {
        super(widget, propSelector);
    }

    @Override
    public IDateTimePickerConfig0<T> skipValidation() {
        skipVal();
        return this;
    }

}
