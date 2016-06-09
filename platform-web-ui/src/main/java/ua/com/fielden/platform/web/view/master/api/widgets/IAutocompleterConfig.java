package ua.com.fielden.platform.web.view.master.api.widgets;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.helpers.ISkipValidation;
import ua.com.fielden.platform.web.view.master.api.widgets.autocompleter.IAutocompleterConfig0;

/**
 * A configuration for an autocompleter that gets associated with a property of an entity type.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IAutocompleterConfig<T extends AbstractEntity<?>> extends IAutocompleterConfig0<T>, ISkipValidation<IAutocompleterConfig0<T>> {
}
