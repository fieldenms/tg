package ua.com.fielden.platform.web.view.master.api.widgets.autocompleter.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

/**
 *
 * A base wrapper for <code>tg-entity-search-criteria</code> or <code>tg-entity-editor</code> that represents a widget for specifying autocompletion queries against a property of
 * an entity type.
 *
 *
 * @author TG Team
 *
 */
public abstract class AbstractEntityAutocompletionWidget extends AbstractWidget {
    @SuppressWarnings("rawtypes")
    private Class<? extends IValueMatcher> matcherType;
    private boolean shouldSearchByDesc = false;
    private final CentreContextConfig centreContextConfig;

    public AbstractEntityAutocompletionWidget(final String widgetPath, final Pair<String, String> titleDesc, final String propertyName, final CentreContextConfig centreContextConfig) {
        super(widgetPath, titleDesc, propertyName);
        this.centreContextConfig = centreContextConfig;
    }

    @Override
    protected Map<String, Object> createCustomAttributes() {
        final Map<String, Object> attrs = new LinkedHashMap<>();
        attrs.put("createModifiedPropertiesHolder", "{{createModifiedPropertiesHolder}}");
        attrs.put("user", "{{user}}");
        attrs.put("entitytype", "{{entitytype}}");
        attrs.put("hightlightDesc", Boolean.toString(shouldSearchByDesc));
        if (centreContextConfig != null) {
            if (centreContextConfig.withSelectionCrit) {
                // disregarded at this stage -- sends every time
            }
            attrs.put("getSelectedEntities", "{{getSelectedEntities}}");
            attrs.put("requireSelectedEntities", centreContextConfig.withCurrentEtity ? "ONE" : (centreContextConfig.withAllSelectedEntities ? "ALL" : "NONE"));
            attrs.put("getMasterEntity", "{{getMasterEntity}}");
            attrs.put("requireMasterEntity", centreContextConfig.withMasterEntity ? "true" : "false");
        }
        return attrs;
    };

    @SuppressWarnings("rawtypes")
    public AbstractEntityAutocompletionWidget setMatcherType(final Class<? extends IValueMatcher> matcherType) {
        this.matcherType = matcherType;
        return this;
    }

    @SuppressWarnings("rawtypes")
    public Class<? extends IValueMatcher> getMatcherType() {
        return matcherType;
    }

    public boolean isShouldSearchByDesc() {
        return shouldSearchByDesc;
    }

    public void setShouldSearchByDesc(final boolean shouldSearchByDesc) {
        this.shouldSearchByDesc = shouldSearchByDesc;
    }

}
