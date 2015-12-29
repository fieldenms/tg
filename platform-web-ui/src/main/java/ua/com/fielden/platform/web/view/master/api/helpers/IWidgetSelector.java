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

    IAutocompleterConfig<T> asAutocompleter();
    ISinglelineTextConfig<T> asSinglelineText();
    IMultilineTextConfig<T> asMultilineText();
    IHiddenTextConfig<T> asHiddenText();
    ICollectionalRepresentorConfig<T> asCollectionalRepresentor();
    ICollectionalEditorConfig<T> asCollectionalEditor(final String chosenIdsPropertyName, final String addedIdsPropertyName, final String removedIdsPropertyName);

    IFileConfig<T> asFile();

    IDateTimePickerConfig<T> asDateTimePicker();
    IDatePickerConfig<T> asDatePicker();
    ITimePickerConfig<T> asTimePicker();

    IDecimalConfig<T> asDecimal();
    ISpinnerConfig<T> asSpinner();
    IMoneyConfig<T> asMoney();
    ICheckboxConfig<T> asCheckbox();

    IPhoneNumberConfig<T> asPhoneNumber();
    IEmailConfig<T> asEmail();
    IColourConfig<T> asColour();

}
