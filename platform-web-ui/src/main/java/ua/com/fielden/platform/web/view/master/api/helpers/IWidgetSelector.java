package ua.com.fielden.platform.web.view.master.api.helpers;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.widgets.IAutocompleterConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.ICheckboxConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.ICollectionalEditorConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.ICollectionalRepresentorConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.IColourConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.IDatePickerConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.IDateTimePickerConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.IDecimalConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.IEmailConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.IFileConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.IHiddenTextConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.IHyperlinkConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.IMoneyConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.IMultilineTextConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.IPhoneNumberConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.ISinglelineTextConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.ISpinnerConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.ITimePickerConfig;

/**
 *
 * Provides a way to specify an editor for a designated property on an entity master.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IWidgetSelector<T extends AbstractEntity<?>> {

    /** An editor for entity-typed properties. */
    IAutocompleterConfig<T> asAutocompleter();
    /** An editor for properties of type String that have not too long values (i.e. can normally fit on a single line). */
    ISinglelineTextConfig<T> asSinglelineText();
    /** An editor for properties of type String that have may have long values, which would benefit from being displayed on several lines. */
    IMultilineTextConfig<T> asMultilineText();
    /** Not-yet supported, use asSinglelineText() instead. */
    IHiddenTextConfig<T> asHiddenText();
    /** An editor for representing short collectional properties (not editable). */
    ICollectionalRepresentorConfig<T> asCollectionalRepresentor();
    /** An editor for representing short collectional properties with editing support. */
    ICollectionalEditorConfig<T> asCollectionalEditor();

    IFileConfig<T> asFile();

    IDateTimePickerConfig<T> asDateTimePicker();
    IDatePickerConfig<T> asDatePicker();
    ITimePickerConfig<T> asTimePicker();

    /** An editor for properties of type BigDecimal. */
    IDecimalConfig<T> asDecimal();
    /** An editor for properties of type Integer. */
    @Deprecated//(since = "2021; use asInteger() instead.")
    ISpinnerConfig<T> asSpinner();
    /** An editor for properties of type Integer. */
    ISpinnerConfig<T> asInteger();
    /** An editor for properties of type Money. */
    IMoneyConfig<T> asMoney();
    /** An editor for properties of type boolean. */
    ICheckboxConfig<T> asCheckbox();

    /** Not-yet supported, use asSinglelineText() instead. */
    IPhoneNumberConfig<T> asPhoneNumber();
    /** Not-yet supported, use asSinglelineText() instead. */
    IEmailConfig<T> asEmail();
    /** An editor for properties of type Colour. */
    IColourConfig<T> asColour();
    /** An editor for properties of type Hyperlink. */
    IHyperlinkConfig<T> asHyperlink();

}
