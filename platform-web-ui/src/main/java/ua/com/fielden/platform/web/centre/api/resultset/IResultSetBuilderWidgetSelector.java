package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;

/// A contract for creating autocompleter for property in EGI
///
public interface IResultSetBuilderWidgetSelector<T extends AbstractEntity<?>> extends IResultSetEditorConfig<T>{

    /// Creates an autocompleter for a property in EGI. The autocompleter’s type matches the property’s type.
    ///
    IResultSetAutocompleterConfig<T> asAutocompleter();

    /// Creates an autocompleter for a property in EGI. The type of the autocompleter is specified by a parameter.
    ///
    IResultSetAutocompleterConfig<T> asAutocompleter(Class<? extends AbstractEntity<?>> entityType);
}
