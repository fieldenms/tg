package ua.com.fielden.platform.web.master.api.helpers;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.master.api.editors.ITextInputConfig;
import ua.com.fielden.platform.web.master.api.editors.autocompleter.IAutocompleterConfig;

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
    ITextInputConfig<T> asTextInput();
}
