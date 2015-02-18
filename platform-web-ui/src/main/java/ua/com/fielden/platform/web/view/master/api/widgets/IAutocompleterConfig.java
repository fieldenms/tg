package ua.com.fielden.platform.web.view.master.api.widgets;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.widgets.autocompleter.IAutocompleterConfig0;

/**
 * A configuration for an autocompleter that gets associated with a property of an entity type.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IAutocompleterConfig<T extends AbstractEntity<?>> extends IAutocompleterConfig0<T> {

    /**
     * This declaration indicates that an asynchronous validation to a corresponding property should be skipped.
     * This should be done for optimisation reasons only in relation to properties that have heavy validation.
     * It should be understood the actual validation would anyway take place upon entity saving.
     *
     * @param matcher
     * @return
     */
    IAutocompleterConfig0<T> skipValidation();
}
