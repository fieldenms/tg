package ua.com.fielden.platform.web.master.api.widgets;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.master.api.widgets.spinner.ISpinnerConfig0;

/**
 *
 * A configuration for a spinner, which should be used as an editor for integer type properties.
 * <p>
 * In case of HTML, some browsers render <code>input</code> fields with attribute <code>type="number"</code> as a spinner.
 * However, even with browsers that do render number inputs as spinners, the actual visual appearance differ significantly.
 * Therefore, it is best to develop a separate spinner component that would a consistent visual representation across the browsers.
 * It should closely follow the same approach as used for the datetime picker and autocompleter widgets.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface ISpinnerConfig<T extends AbstractEntity<?>> extends ISpinnerConfig0<T> {
    /**
     * This declaration indicates that an asynchronous validation to a corresponding property should be skipped.
     * This should be done for optimisation reasons only in relation to properties that have heavy validation.
     * It should be understood the actual validation would anyway take place upon entity saving.
     *
     * @param matcher
     * @return
     */
    ISpinnerConfig0<T> skipValidation();
}
