package ua.com.fielden.platform.web.master.api.editors.autocompleter;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.master.api.helpers.IAlso;
import ua.com.fielden.platform.web.master.api.helpers.ILayoutConfig;

public interface IAutocompleterConfig0<T extends AbstractEntity<?>> extends IAlso<T>, ILayoutConfig {
    /** Indicates whether description should also be included as part of search. */
    IAutocompleterConfig1<T> byDesc();

    /** Excludes key and searches by desc only. */
    IAutocompleterConfig1<T> byDescOnly();
}