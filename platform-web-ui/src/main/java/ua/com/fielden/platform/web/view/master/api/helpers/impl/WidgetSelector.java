package ua.com.fielden.platform.web.view.master.api.helpers.impl;

import org.apache.commons.lang3.StringUtils;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DateOnly;
import ua.com.fielden.platform.entity.annotation.TimeOnly;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.serialisation.jackson.DefaultValueContract;
import ua.com.fielden.platform.web.view.master.api.helpers.IWidgetSelector;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;
import ua.com.fielden.platform.web.view.master.api.widgets.*;
import ua.com.fielden.platform.web.view.master.api.widgets.autocompleter.impl.EntityAutocompletionWidget;
import ua.com.fielden.platform.web.view.master.api.widgets.checkbox.impl.CheckboxWidget;
import ua.com.fielden.platform.web.view.master.api.widgets.collectional.impl.CollectionalEditorWidget;
import ua.com.fielden.platform.web.view.master.api.widgets.collectional.impl.CollectionalRepresentorWidget;
import ua.com.fielden.platform.web.view.master.api.widgets.colour.impl.ColourWidget;
import ua.com.fielden.platform.web.view.master.api.widgets.datetimepicker.impl.DateTimePickerWidget;
import ua.com.fielden.platform.web.view.master.api.widgets.decimal.impl.DecimalWidget;
import ua.com.fielden.platform.web.view.master.api.widgets.hyperlink.impl.HyperlinkWidget;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.*;
import ua.com.fielden.platform.web.view.master.api.widgets.money.impl.MoneyWidget;
import ua.com.fielden.platform.web.view.master.api.widgets.multilinetext.impl.MultilineTextWidget;
import ua.com.fielden.platform.web.view.master.api.widgets.richtext.impl.RichTextWidget;
import ua.com.fielden.platform.web.view.master.api.widgets.singlelinetext.impl.SinglelineTextWidget;
import ua.com.fielden.platform.web.view.master.api.widgets.spinner.impl.SpinnerWidget;
import ua.com.fielden.platform.web.view.master.exceptions.EntityMasterConfigurationException;

import java.util.Optional;

import static java.lang.String.format;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.getTimePortionToDisplay;

public class WidgetSelector<T extends AbstractEntity<?>> implements IWidgetSelector<T> {

    private static final String ERR_INVALID_AUTOCOMPLETER_TYPE = """
            Type [%s] cannot be used for autocompletion of property [%s.%s] with type [%s]. \
            Please use asAutocompleter(entityType), or asAutocompleter() for an entity-typed property.""";
    private static final String ERR_INVALID_PROPERTY_FOR_AUTOCOMPLETION = """
            Property [%s.%s] with type [%s] cannot be used for autocompletion. \
            Please use asAutocompleter(entityType), or asAutocompleter() for an entity-typed property.""";
    private static final String ERR_INVALID_DATEPICKER_CHOICE = "Invalid editor choice for property [%s.%s] due to annotation @%s.";

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
        widget = createAutocompleter(smBuilder.getEntityType(), propertyName, Optional.empty());
        return new EntityAutocompletionConfig<>((EntityAutocompletionWidget) widget, smBuilder, withMatcherCallbank);
    }

    @Override
    public IAutocompleterConfig<T> asAutocompleter(final Class<? extends AbstractEntity<?>> entityType) {
        widget = createAutocompleter(smBuilder.getEntityType(), propertyName, Optional.ofNullable(entityType));
        return new EntityAutocompletionConfig<>((EntityAutocompletionWidget) widget, smBuilder, withMatcherCallbank);
    }

    public static <T extends AbstractEntity<?>> EntityAutocompletionWidget createAutocompleter(final Class<T> entityType, final String propertyName, final Optional<Class<? extends AbstractEntity<?>>> optPropType) {
        final var declaredPropType = StringUtils.isEmpty(propertyName) ? entityType : PropertyTypeDeterminator.determinePropertyType(entityType, propertyName);
        return optPropType.map(propType -> {
            if (String.class.isAssignableFrom(declaredPropType)) {
                return new EntityAutocompletionWidget(TitlesDescsGetter.getTitleAndDesc(propertyName, entityType), propertyName, propType, true);
            } else if (propType.equals(declaredPropType)) {
                return new EntityAutocompletionWidget(TitlesDescsGetter.getTitleAndDesc(propertyName, entityType), propertyName, propType, false);
            } else {
                throw new EntityMasterConfigurationException(format(ERR_INVALID_AUTOCOMPLETER_TYPE, propType.getTypeName(), entityType.getSimpleName(), propertyName, declaredPropType.getTypeName()));
            }
        }).orElseGet(() -> {
            if (AbstractEntity.class.isAssignableFrom(declaredPropType)) {
                return new EntityAutocompletionWidget(TitlesDescsGetter.getTitleAndDesc(propertyName, entityType), propertyName, (Class<? extends AbstractEntity<?>>) declaredPropType, false);
            } else {
                throw new EntityMasterConfigurationException(format(ERR_INVALID_PROPERTY_FOR_AUTOCOMPLETION, entityType.getSimpleName(), propertyName, declaredPropType.getTypeName()));
            }
        });
    }

    @Override
    public ISinglelineTextConfig<T> asSinglelineText() {
        widget = new SinglelineTextWidget(TitlesDescsGetter.getTitleAndDesc(propertyName, smBuilder.getEntityType()), propertyName);
        return new SinglelineTextConfig<>((SinglelineTextWidget) widget, smBuilder);
    }

    @Override
    public ICollectionalRepresentorConfig<T> asCollectionalRepresentor() {
        widget = new CollectionalRepresentorWidget(TitlesDescsGetter.getTitleAndDesc(propertyName, smBuilder.getEntityType()), propertyName);
        return new CollectionalRepresentorConfig<>((CollectionalRepresentorWidget) widget, smBuilder);
    }

    @Override
    public ICollectionalEditorConfig<T> asCollectionalEditor() {
        widget = new CollectionalEditorWidget(TitlesDescsGetter.getTitleAndDesc(propertyName, smBuilder.getEntityType()), propertyName);
        return new CollectionalEditorConfig<>((CollectionalEditorWidget) widget, smBuilder);
    }

    @Override
    public IMultilineTextConfig<T> asMultilineText() {
        widget = new MultilineTextWidget(TitlesDescsGetter.getTitleAndDesc(propertyName, smBuilder.getEntityType()), smBuilder.getEntityType(), propertyName);
        return new MultilineTextConfig<>((MultilineTextWidget) widget, smBuilder);
    }

    @Override
    public IRichTextConfig<T> asRichText() {
        widget = new RichTextWidget(TitlesDescsGetter.getTitleAndDesc(propertyName, smBuilder.getEntityType()), smBuilder.getEntityType(), propertyName);
        return new RichTextConfig<>((RichTextWidget)widget, smBuilder);
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
        final String timePortion = getTimePortionToDisplay(smBuilder.getEntityType(), propertyName);
        if (timePortion == null) {
            widget = new DateTimePickerWidget(
                    TitlesDescsGetter.getTitleAndDesc(propertyName, smBuilder.getEntityType()),
                    propertyName,
                    false,
                    DefaultValueContract.getTimeZone(smBuilder.getEntityType(), propertyName),
                    null
                    );
            return new DateTimePickerConfig<>((DateTimePickerWidget) widget, smBuilder);
        }
        throw new EntityMasterConfigurationException(format(ERR_INVALID_DATEPICKER_CHOICE,
                smBuilder.getEntityType().getSimpleName(), propertyName, "DATE".equals(timePortion) ? DateOnly.class.getSimpleName() : TimeOnly.class.getSimpleName()));
    }

    @Override
    public IDatePickerConfig<T> asDatePicker() {
        final String DATE_ONLY = "DATE";
        if (DATE_ONLY.equals(getTimePortionToDisplay(smBuilder.getEntityType(), propertyName))) {
            widget = new DateTimePickerWidget(
                    TitlesDescsGetter.getTitleAndDesc(propertyName, smBuilder.getEntityType()),
                    propertyName,
                    false,
                    DefaultValueContract.getTimeZone(smBuilder.getEntityType(), propertyName),
                    DATE_ONLY
                    );
            return new DatePickerConfig<>((DateTimePickerWidget) widget, smBuilder);
        }
        throw new EntityMasterConfigurationException(format(ERR_INVALID_DATEPICKER_CHOICE, propertyName, smBuilder.getEntityType().getSimpleName(), DateOnly.class.getSimpleName()));
    }

    @Override
    public ITimePickerConfig<T> asTimePicker() {
        final String TIME_ONLY = "TIME";
        if (TIME_ONLY.equals(getTimePortionToDisplay(smBuilder.getEntityType(), propertyName))) {
            widget = new DateTimePickerWidget(
                    TitlesDescsGetter.getTitleAndDesc(propertyName, smBuilder.getEntityType()),
                    propertyName,
                    false,
                    DefaultValueContract.getTimeZone(smBuilder.getEntityType(), propertyName),
                    TIME_ONLY
                    );
            return new TimePickerConfig<>((DateTimePickerWidget) widget, smBuilder);
        }
        throw new EntityMasterConfigurationException(format(ERR_INVALID_DATEPICKER_CHOICE, propertyName, smBuilder.getEntityType().getSimpleName(), TimeOnly.class.getSimpleName()));
    }

    @Override
    public IDecimalConfig<T> asDecimal() {
        widget = new DecimalWidget(TitlesDescsGetter.getTitleAndDesc(propertyName, smBuilder.getEntityType()), propertyName);
        return new DecimalConfig<>((DecimalWidget) widget, smBuilder);
    }

    @Override
    public ISpinnerConfig<T> asSpinner() {
        widget = new SpinnerWidget(TitlesDescsGetter.getTitleAndDesc(propertyName, smBuilder.getEntityType()), propertyName);
        return new SpinnerConfig<>((SpinnerWidget) widget, smBuilder);
    }

    @Override
    public ISpinnerConfig<T> asInteger() {
        return asSpinner();
    }

    @Override
    public IMoneyConfig<T> asMoney() {
        widget = new MoneyWidget(TitlesDescsGetter.getTitleAndDesc(propertyName, smBuilder.getEntityType()), propertyName);
        return new MoneyConfig<>((MoneyWidget) widget, smBuilder);
    }

    @Override
    public ICheckboxConfig<T> asCheckbox() {
        widget = new CheckboxWidget(TitlesDescsGetter.getTitleAndDesc(propertyName, smBuilder.getEntityType()), propertyName);
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
        widget = new ColourWidget(TitlesDescsGetter.getTitleAndDesc(propertyName, smBuilder.getEntityType()), propertyName);
        return new ColourConfig<>((ColourWidget) widget, smBuilder);
    }

    @Override
    public IHyperlinkConfig<T> asHyperlink() {
        widget = new HyperlinkWidget(TitlesDescsGetter.getTitleAndDesc(propertyName, smBuilder.getEntityType()), propertyName);
        return new HyperlinkConfig<>((HyperlinkWidget) widget, smBuilder);
    }

    public AbstractWidget widget() {
        return widget;
    }
}
