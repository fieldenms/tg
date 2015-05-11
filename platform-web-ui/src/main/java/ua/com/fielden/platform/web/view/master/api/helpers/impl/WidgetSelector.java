package ua.com.fielden.platform.web.view.master.api.helpers.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.web.view.master.api.helpers.IWidgetSelector;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;
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
import ua.com.fielden.platform.web.view.master.api.widgets.autocompleter.impl.EntityAutocompletionWidget;
import ua.com.fielden.platform.web.view.master.api.widgets.checkbox.impl.CheckboxWidget;
import ua.com.fielden.platform.web.view.master.api.widgets.datetimepicker.impl.DateTimePickerWidget;
import ua.com.fielden.platform.web.view.master.api.widgets.decimal.impl.DecimalWidget;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.CheckboxConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.DateTimePickerConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.DecimalConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.EntityAutocompletionConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.MultilineTextConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.SinglelineTextConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.SpinnerConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.multilinetext.impl.MultilineTextWidget;
import ua.com.fielden.platform.web.view.master.api.widgets.singlelinetext.impl.SinglelineTextWidget;
import ua.com.fielden.platform.web.view.master.api.widgets.spinner.impl.SpinnerWidget;

public class WidgetSelector<T extends AbstractEntity<?>> implements IWidgetSelector<T> {

    public final SimpleMasterBuilder<T> smBuilder;
    public final String propertyName;

    private AbstractWidget widget;

    private final SimpleMasterBuilder<T>.WithMatcherCallback withMatcherCallbank;

    public WidgetSelector(
            final SimpleMasterBuilder<T> simpleMaster,
            final String propertyName,
            final SimpleMasterBuilder<T>.WithMatcherCallback withMatcherCallbank) {
        this.smBuilder = simpleMaster;
        this.propertyName = propertyName;
        this.withMatcherCallbank = withMatcherCallbank;
    }

    public WidgetSelector(
            final SimpleMasterBuilder<T> simpleMaster,
            final String propertyName) {
        this(simpleMaster, propertyName, null);
    }

    @Override
    public IAutocompleterConfig<T> asAutocompleter() {
        widget = new EntityAutocompletionWidget(TitlesDescsGetter.getTitleAndDesc(propertyName, smBuilder.entityType), propertyName, null, false);
        return new EntityAutocompletionConfig<>((EntityAutocompletionWidget) widget, smBuilder, withMatcherCallbank);
    }

    @Override
    public ISinglelineTextConfig<T> asSinglelineText() {
        widget = new SinglelineTextWidget(TitlesDescsGetter.getTitleAndDesc(propertyName, smBuilder.entityType), propertyName);
        return new SinglelineTextConfig<>((SinglelineTextWidget) widget, smBuilder);
    }

    @Override
    public IMultilineTextConfig<T> asMultilineText() {
        widget = new MultilineTextWidget(TitlesDescsGetter.getTitleAndDesc(propertyName, smBuilder.entityType), propertyName);
        return new MultilineTextConfig<>((MultilineTextWidget) widget, smBuilder);
    }

    @Override
    public IHiddenTextConfig<T> asHiddenText() {
        throw new UnsupportedOperationException("HiddenText widget is not yet supported.");
    }

    @Override
    public IFileConfig<T> asFile() {
        throw new UnsupportedOperationException("File widget is not yet supported.");
    }

    @Override
    public IDateTimePickerConfig<T> asDateTimePicker() {
        widget = new DateTimePickerWidget(TitlesDescsGetter.getTitleAndDesc(propertyName, smBuilder.entityType), propertyName);
        return new DateTimePickerConfig<>((DateTimePickerWidget) widget, smBuilder);
    }

    @Override
    public IDatePickerConfig<T> asDatePicker() {
        throw new UnsupportedOperationException("DatePicker widget is not yet supported.");
    }

    @Override
    public ITimePickerConfig<T> asTimePicker() {
        throw new UnsupportedOperationException("TimePicker widget is not yet supported.");
    }

    @Override
    public IDecimalConfig<T> asDecimal() {
        widget = new DecimalWidget(TitlesDescsGetter.getTitleAndDesc(propertyName, smBuilder.entityType), propertyName);
        return new DecimalConfig<>((DecimalWidget) widget, smBuilder);
    }

    @Override
    public ISpinnerConfig<T> asSpinner() {
        widget = new SpinnerWidget(TitlesDescsGetter.getTitleAndDesc(propertyName, smBuilder.entityType), propertyName);
        return new SpinnerConfig<>((SpinnerWidget) widget, smBuilder);
    }

    @Override
    public IMoneyConfig<T> asMoney() {
        throw new UnsupportedOperationException("Money widget is not yet supported.");
    }

    @Override
    public ICheckboxConfig<T> asCheckbox() {
        widget = new CheckboxWidget(TitlesDescsGetter.getTitleAndDesc(propertyName, smBuilder.entityType), propertyName);
        return new CheckboxConfig<>((CheckboxWidget) widget, smBuilder);
    }

    @Override
    public IPhoneNumberConfig<T> asPhoneNumber() {
        throw new UnsupportedOperationException("PhoneNumber widget is not yet supported.");
    }

    @Override
    public IEmailConfig<T> asEmail() {
        throw new UnsupportedOperationException("Email widget is not yet supported.");
    }

    @Override
    public IColourConfig<T> asColour() {
        throw new UnsupportedOperationException("ColourPicker widget is not yet supported.");
    }

    public AbstractWidget widget() {
        return widget;
    }
}
