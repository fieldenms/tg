package ua.com.fielden.platform.web.centre.api.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetAutocompleterConfig;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetAutocompleterConfigAdditionalProps;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetAutocompleterWithMatcher;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder3Ordering;
import ua.com.fielden.platform.web.view.master.api.widgets.autocompleter.impl.EntityAutocompletionWidget;

public class ResultSetAutocompleterConfig<T extends AbstractEntity<?>> extends ResultSetBuilderWrapperForEditors<T> implements IResultSetAutocompleterConfig<T> {

    private final EntityAutocompletionWidget widget;

    public ResultSetAutocompleterConfig(final ResultSetBuilder<T> builder, final EntityAutocompletionWidget widget) {
        super(builder);
        this.widget = widget;
    }

    @Override
    public IResultSetAutocompleterWithMatcher<T> skipValidation() {
        widget.skipValidation();
        return this;
    }

    @Override
    public IResultSetAutocompleterConfigAdditionalProps<T> withMatcher(final Class<? extends IValueMatcherWithContext<T, ?>> matcherType) {
        builder.assignMatcher(widget.propertyName(), matcherType);
        widget.setMatcherType(matcherType);
        return this;
    }

    @Override
    public IResultSetBuilder3Ordering<T> lightDesc() {
        widget.setLightDesc(true);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public IResultSetBuilder3Ordering<T> withProps(final Pair<String, Boolean> propNameAndLightOption, final Pair<String, Boolean>... morePropNameAndLightOption) {
        final List<Pair<String, Boolean>> pairs = new ArrayList<>();
        pairs.add(propNameAndLightOption);
        pairs.addAll(Arrays.asList(morePropNameAndLightOption));
        widget.setAdditionalProps(pairs);
        return this;
    }
}
