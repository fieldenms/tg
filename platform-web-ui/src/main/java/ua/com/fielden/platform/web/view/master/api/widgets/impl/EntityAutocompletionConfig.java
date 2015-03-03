package ua.com.fielden.platform.web.view.master.api.widgets.impl;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.helpers.IPropertySelector;
import ua.com.fielden.platform.web.view.master.api.widgets.IAutocompleterConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.autocompleter.IAutocompleterConfig0;
import ua.com.fielden.platform.web.view.master.api.widgets.autocompleter.IAutocompleterConfig1;
import ua.com.fielden.platform.web.view.master.api.widgets.autocompleter.IAutocompleterConfig2;
import ua.com.fielden.platform.web.view.master.api.widgets.autocompleter.impl.EntityAutocompletionWidget;

public class EntityAutocompletionConfig<T extends AbstractEntity<?>>
        extends AbstractEditorWidgetConfig<T, EntityAutocompletionWidget, IAutocompleterConfig0<T>>
        implements IAutocompleterConfig<T>, IAutocompleterConfig2<T> {

    public EntityAutocompletionConfig(final EntityAutocompletionWidget widget, final IPropertySelector<T> propSelector) {
        super(widget, propSelector);
    }
    @Override
    public IAutocompleterConfig1<T> withMatcher(final Class<IValueMatcher> matcherType) {
        widget().setMatcherType(matcherType);
        return this;
    }

    @Override
    public IAutocompleterConfig2<T> byDesc() {
        widget().setShouldSearchByDesc(true);
        return this;
    }

    @Override
    public IAutocompleterConfig2<T> byDescOnly() {
        widget().setShouldSearchByDescOnly(true);
        return this;
    }

    @Override
    public IAutocompleterConfig0<T> skipValidation() {
        skipVal();
        return this;
    }
}
