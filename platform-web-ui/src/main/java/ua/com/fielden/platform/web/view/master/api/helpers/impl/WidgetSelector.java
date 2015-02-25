package ua.com.fielden.platform.web.view.master.api.helpers.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.helpers.IWidgetSelector;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMaster;
import ua.com.fielden.platform.web.view.master.api.widgets.IAutocompleterConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.ICheckboxConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.IColourConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.IDatePickerConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.IDateTimePickerConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.IDecimalConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.IEmailConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.IFileConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.IHiddenTextConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.IMoneyConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.IMultilineTextConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.IPhoneNumberConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.ISinglelineTextConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.ISpinnerConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.ITimePickerConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.checkbox.impl.CheckboxWidget;
import ua.com.fielden.platform.web.view.master.api.widgets.datetimepicker.impl.DateTimePickerWidget;
import ua.com.fielden.platform.web.view.master.api.widgets.decimal.impl.DecimalWidget;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.CheckboxConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.DateTimePickerConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.DecimalConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.MultilineTextConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.SinglelineTextConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.multilinetext.impl.MultilineTextWidget;
import ua.com.fielden.platform.web.view.master.api.widgets.singlelinetext.impl.SinglelineTextWidget;

public class WidgetSelector<T extends AbstractEntity<?>> implements IWidgetSelector<T> {

    public final SimpleMaster<T> simpleMaster;
    public final String propertyName;

    private AbstractWidget widget;

    public WidgetSelector(final SimpleMaster<T> simpleMaster, final String propertyName) {
        this.simpleMaster = simpleMaster;
        this.propertyName = propertyName;
    }

    @Override
    public IAutocompleterConfig<T> asAutocompleter() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ISinglelineTextConfig<T> asSinglelineText() {
        widget = new SinglelineTextWidget(simpleMaster.entityType, propertyName);
        return new SinglelineTextConfig<>((SinglelineTextWidget) widget, simpleMaster);
    }

    @Override
    public IMultilineTextConfig<T> asMultilineText() {
        widget = new MultilineTextWidget(simpleMaster.entityType, propertyName);
        return new MultilineTextConfig<>((MultilineTextWidget) widget, simpleMaster);
    }

    @Override
    public IHiddenTextConfig<T> asHiddenText() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IFileConfig<T> asFile() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IDateTimePickerConfig<T> asDateTimePicker() {
        widget = new DateTimePickerWidget(simpleMaster.entityType, propertyName);
        return new DateTimePickerConfig<>((DateTimePickerWidget) widget, simpleMaster);
    }

    @Override
    public IDatePickerConfig<T> asDatePicker() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ITimePickerConfig<T> asTimePicker() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IDecimalConfig<T> asDecimal() {
        widget = new DecimalWidget(simpleMaster.entityType, propertyName);
        return new DecimalConfig<>((DecimalWidget) widget, simpleMaster);
    }

    @Override
    public ISpinnerConfig<T> asSpinner() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IMoneyConfig<T> asMoney() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ICheckboxConfig<T> asCheckbox() {
        widget = new CheckboxWidget(simpleMaster.entityType, propertyName);
        return new CheckboxConfig<>((CheckboxWidget) widget, simpleMaster);
    }

    @Override
    public IPhoneNumberConfig<T> asPhoneNumber() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IEmailConfig<T> asEmail() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IColourConfig<T> asColour() {
        // TODO Auto-generated method stub
        return null;
    }

    public AbstractWidget widget() {
        return widget;
    }
}
