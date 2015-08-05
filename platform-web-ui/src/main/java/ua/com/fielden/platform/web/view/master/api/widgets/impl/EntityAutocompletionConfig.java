package ua.com.fielden.platform.web.view.master.api.widgets.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.view.master.api.helpers.IPropertySelector;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;
import ua.com.fielden.platform.web.view.master.api.widgets.IAutocompleterConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.autocompleter.IAutocompleterConfig0;
import ua.com.fielden.platform.web.view.master.api.widgets.autocompleter.IAutocompleterConfig1;
import ua.com.fielden.platform.web.view.master.api.widgets.autocompleter.IAutocompleterConfig2;
import ua.com.fielden.platform.web.view.master.api.widgets.autocompleter.impl.EntityAutocompletionWidget;

public class EntityAutocompletionConfig<T extends AbstractEntity<?>>
        extends AbstractEditorWidgetConfig<T, EntityAutocompletionWidget, IAutocompleterConfig0<T>>
        implements IAutocompleterConfig<T>, IAutocompleterConfig2<T> {

    private final SimpleMasterBuilder<T>.WithMatcherCallback withMatcherCallbank;

    public EntityAutocompletionConfig(//
            final EntityAutocompletionWidget widget,//
            final IPropertySelector<T> propSelector,//
            final SimpleMasterBuilder<T>.WithMatcherCallback withMatcherCallbank) {
        super(widget, propSelector);
        if (withMatcherCallbank == null) {
            throw new IllegalArgumentException("Value matcher setup callback should not be null.");
        }
        this.withMatcherCallbank = withMatcherCallbank;
    }

    @Override
    public IAutocompleterConfig1<T> withMatcher(final Class<? extends IValueMatcherWithContext<T, ?>> matcherType) {
        widget().setMatcherType(matcherType);
        withMatcherCallbank.assign(widget().propertyName() , matcherType);
        return this;
    }

    @Override
    public IAutocompleterConfig2<T> lightDesc() {
        widget().setLightDesc(true);
        return this;
    }


    @Override
    public IAutocompleterConfig0<T> skipValidation() {
        skipVal();
        return this;
    }

    @SafeVarargs
    @Override
    public final IAutocompleterConfig2<T> withProps(final Pair<String, Boolean> propNameAndLightOption, final Pair<String, Boolean>... morePropNameAndLightOption) {
        final List<Pair<String, Boolean>> pairs = new ArrayList<>();
        pairs.add(propNameAndLightOption);
        pairs.addAll(Arrays.asList(morePropNameAndLightOption));
        widget().setAdditionalProps(pairs);
        return this;
    }
}
