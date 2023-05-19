package ua.com.fielden.platform.web.centre.api.crit;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
* A contract for specifying properties whether to turn off ability to filter values by 'active only' toggle button.
*
* @author TG Team
*
* @param <T>
*/
public interface ISingleValueAutocompleterBuilder0<T extends AbstractEntity<?>, V extends AbstractEntity<?>> extends ISingleValueAutocompleterBuilder1<T, V> {

    /**
     * Turns off ability to filter values by 'active only' toggle button in autocompleter result dialog.
     * May be useful for autocompleters with custom matcher, that already filters out inactive values.
     */
    ISingleValueAutocompleterBuilder1<T, V> withoutActiveOnlyOption();

}