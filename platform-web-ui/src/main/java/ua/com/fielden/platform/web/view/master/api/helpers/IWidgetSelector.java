package ua.com.fielden.platform.web.view.master.api.helpers;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.widgets.*;

/// Provides a way to specify an editor for a designated property on an entity master.
///
public interface IWidgetSelector<T extends AbstractEntity<?>> {

    /// An editor for entity-typed properties.
    IAutocompleterConfig<T> asAutocompleter();
    /// An editor for String-typed properties with `entityType` used for autocompletion.
    IAutocompleterConfig<T> asAutocompleter(Class<? extends AbstractEntity<?>> entityType);
    /// An editor for properties of type String that have not too long values (i.e. can normally fit on a single line).
    ISinglelineTextConfig<T> asSinglelineText();
    /// An editor for properties of type String that may have long values, which would benefit from being displayed on several lines.
    IMultilineTextConfig<T> asMultilineText();
    /// An editor for properties of type RichText.
    IRichTextConfig<T> asRichText();
    /// Not-yet supported, use asSinglelineText() instead.
    IHiddenTextConfig<T> asHiddenText();
    /// An editor for representing short collectional properties (not editable).
    ICollectionalRepresentorConfig<T> asCollectionalRepresentor();
    /// An editor for representing short collectional properties with editing support.
    ICollectionalEditorConfig<T> asCollectionalEditor();

    IFileConfig<T> asFile();

    IDateTimePickerConfig<T> asDateTimePicker();
    IDatePickerConfig<T> asDatePicker();
    ITimePickerConfig<T> asTimePicker();

    /// An editor for properties of type BigDecimal.
    IDecimalConfig<T> asDecimal();
    /// An editor for properties of type Integer.
    @Deprecated//(since = "2021; use asInteger() instead.")
    ISpinnerConfig<T> asSpinner();
    /// An editor for properties of type Integer.
    ISpinnerConfig<T> asInteger();
    /// An editor for properties of type Money.
    IMoneyConfig<T> asMoney();
    /// An editor for properties of type boolean.
    ICheckboxConfig<T> asCheckbox();

    /// Not-yet supported, use asSinglelineText() instead.
    IPhoneNumberConfig<T> asPhoneNumber();
    /// Not-yet supported, use asSinglelineText() instead.
    IEmailConfig<T> asEmail();
    /// An editor for properties of type Colour.
    IColourConfig<T> asColour();
    /// An editor for properties of type Hyperlink.
    IHyperlinkConfig<T> asHyperlink();

}
