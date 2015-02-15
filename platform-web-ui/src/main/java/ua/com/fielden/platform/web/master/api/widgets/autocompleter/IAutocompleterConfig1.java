package ua.com.fielden.platform.web.master.api.widgets.autocompleter;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.master.api.actions.IPropertyActionConfig;
import ua.com.fielden.platform.web.master.api.helpers.IAlso;

public interface IAutocompleterConfig1<T extends AbstractEntity<?>> extends IAlso<T>, IPropertyActionConfig<T> {
    /** Indicates whether description should also be included as part of search. */
    IAutocompleterConfig2<T> byDesc();

    /** Excludes key and searches by desc only. */
    IAutocompleterConfig2<T> byDescOnly();
}