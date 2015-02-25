package ua.com.fielden.platform.web.view.master.api.widgets.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.actions.property.IPropertyActionConfig0;
import ua.com.fielden.platform.web.view.master.api.actions.property.impl.PropertyActionConfig;
import ua.com.fielden.platform.web.view.master.api.helpers.IPropertySelector;
import ua.com.fielden.platform.web.view.master.api.widgets.IDateTimePickerConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.datetimepicker.IDateTimePickerConfig0;
import ua.com.fielden.platform.web.view.master.api.widgets.datetimepicker.impl.DateTimePickerWidget;

public class DateTimePickerConfig<T extends AbstractEntity<?>> implements IDateTimePickerConfig<T>, IDateTimePickerConfig0<T> {

    private final IPropertySelector<T> propSelector;
    private final DateTimePickerWidget widget;

    public DateTimePickerConfig(final DateTimePickerWidget widget, final IPropertySelector<T> propSelector) {
        this.propSelector = propSelector;
        this.widget = widget;
    }

    @Override
    public IPropertySelector<T> also() {
        return propSelector;
    }

    @Override
    public IPropertyActionConfig0<T> withAction(final String name, final Class<? extends AbstractEntity<?>> functionalEntity) {
        return new PropertyActionConfig<>(widget.initAction(name, functionalEntity), propSelector);
    }

    @Override
    public IDateTimePickerConfig0<T> skipValidation() {
        this.widget.skipValidation();
        return this;
    }

}
