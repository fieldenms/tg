package ua.com.fielden.platform.web.master.api.widgets.autocompleter;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.master.api.actions.IPropertyActionConfig;
import ua.com.fielden.platform.web.master.api.helpers.IAlso;

public interface IAutocompleterConfig0<T extends AbstractEntity<?>> extends IAlso<T>, IPropertyActionConfig<T> {
    /** Indicates whether description should also be included as part of search. */
    IAutocompleterConfig1<T> byDesc();

    /** Excludes key and searches by desc only. */
    IAutocompleterConfig1<T> byDescOnly();
}