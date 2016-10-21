package ua.com.fielden.platform.web.view.master.api.widgets.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.helpers.IPropertySelector;
import ua.com.fielden.platform.web.view.master.api.widgets.ITimePickerConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.datetimepicker.impl.DateTimePickerWidget;
import ua.com.fielden.platform.web.view.master.api.widgets.timepicker.ITimePickerConfig0;

public class TimePickerConfig<T extends AbstractEntity<?>>
        extends AbstractEditorWidgetConfig<T, DateTimePickerWidget, ITimePickerConfig0<T>>
        implements ITimePickerConfig<T>, ITimePickerConfig0<T> {

    public TimePickerConfig(final DateTimePickerWidget widget, final IPropertySelector<T> propSelector) {
        super(widget, propSelector);
    }

    @Override
    public ITimePickerConfig0<T> skipValidation() {
        skipVal();
        return this;
    }

}
