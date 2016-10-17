package ua.com.fielden.platform.web.view.master.api.widgets.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.helpers.IPropertySelector;
import ua.com.fielden.platform.web.view.master.api.widgets.IDatePickerConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.datepicker.IDatePickerConfig0;
import ua.com.fielden.platform.web.view.master.api.widgets.datetimepicker.impl.DateTimePickerWidget;

public class DatePickerConfig<T extends AbstractEntity<?>>
        extends AbstractEditorWidgetConfig<T, DateTimePickerWidget, IDatePickerConfig0<T>>
        implements IDatePickerConfig<T>, IDatePickerConfig0<T> {

    public DatePickerConfig(final DateTimePickerWidget widget, final IPropertySelector<T> propSelector) {
        super(widget, propSelector);
    }

    @Override
    public IDatePickerConfig0<T> skipValidation() {
        skipVal();
        return this;
    }

}
