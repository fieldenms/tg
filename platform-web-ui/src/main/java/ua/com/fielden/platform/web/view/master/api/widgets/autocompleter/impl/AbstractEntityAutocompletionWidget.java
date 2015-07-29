package ua.com.fielden.platform.web.view.master.api.widgets.autocompleter.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.crit.impl.EntitySingleCritAutocompletionWidget;
import ua.com.fielden.platform.web.centre.widgets.EntityCritAutocompletionWidget;
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
    private final boolean selectionCriteriaWidget;

    public AbstractEntityAutocompletionWidget(final String widgetPath, final Pair<String, String> titleDesc, final String propertyName, final boolean selectionCriteriaWidget) {
        super(widgetPath, titleDesc, propertyName);
        this.selectionCriteriaWidget = selectionCriteriaWidget;
    }

    @Override
    protected Map<String, Object> createCustomAttributes() {
        final Map<String, Object> attrs = new LinkedHashMap<>();

        attrs.put("autocompletion-type", "[[" + (selectionCriteriaWidget ? "miType" : "entityType") + "]]");

        // TODO please implement StringBuilder that aggregates all composite key members into 'additionalProperties' (and incorporate 'desc' as below).
//        if (shouldSearchByDesc) { // FIXME JSON should be generated in single quotes
//            attrs.put("additional-properties", "{desc:true}");
//        } else {
//            attrs.put("additional-properties", "{desc:false}");
//        }
        attrs.put("process-response", "[[_processResponse]]");
        attrs.put("process-error", "[[_processError]]");
        attrs.put("post-searched-default-error", "[[_postSearchedDefaultError]]");
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

    /**
     * Adds the bindings for centre context (if it is not empty).
     *
     * Applicable only for {@link EntityCritAutocompletionWidget} and {@link EntitySingleCritAutocompletionWidget}.
     *
     * @param attrs
     * @param centreContextConfig
     */
    protected void addCentreContextBindings(final Map<String, Object> attrs, final CentreContextConfig centreContextConfig) {
        if (centreContextConfig != null) {
            attrs.put("create-modified-properties-holder", "[[_createModifiedPropertiesHolder]]");
            attrs.put("require-selection-criteria", centreContextConfig.withSelectionCrit ? "true" : "false");
            attrs.put("get-selected-entities", "[[getSelectedEntities]]");
            attrs.put("require-selected-entities", centreContextConfig.withCurrentEtity ? "ONE" : (centreContextConfig.withAllSelectedEntities ? "ALL" : "NONE"));
            attrs.put("get-master-entity", "[[getMasterEntity]]");
            attrs.put("require-master-entity", centreContextConfig.withMasterEntity ? "true" : "false");
        }
    }
}
