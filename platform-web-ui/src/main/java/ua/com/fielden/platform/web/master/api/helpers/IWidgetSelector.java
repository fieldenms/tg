package ua.com.fielden.platform.web.master.api.helpers;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.master.api.editors.IAutocompleterConfig;
import ua.com.fielden.platform.web.master.api.editors.ICheckboxConfig;
import ua.com.fielden.platform.web.master.api.editors.IDatePickerConfig;
import ua.com.fielden.platform.web.master.api.editors.IDateTimePickerConfig;
import ua.com.fielden.platform.web.master.api.editors.IDecimalConfig;
import ua.com.fielden.platform.web.master.api.editors.ISpinnerConfig;
import ua.com.fielden.platform.web.master.api.editors.IMoneyConfig;
import ua.com.fielden.platform.web.master.api.editors.IMultilineTextConfig;
import ua.com.fielden.platform.web.master.api.editors.ITextInputConfig;
import ua.com.fielden.platform.web.master.api.editors.ITimePickerConfig;

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
    ITextInputConfig<T> asSinglelineText();
    IMultilineTextConfig<T> asMultilineText();
    IDateTimePickerConfig<T> asDateTimePicker();
    IDatePickerConfig<T> asDatePicker();
    ITimePickerConfig<T> asTimePicker();
    IDecimalConfig<T> asDecimal();
    ISpinnerConfig<T> asSpinner();
    IMoneyConfig<T> asMoney();
    ICheckboxConfig<T> asCheckbox();

}
