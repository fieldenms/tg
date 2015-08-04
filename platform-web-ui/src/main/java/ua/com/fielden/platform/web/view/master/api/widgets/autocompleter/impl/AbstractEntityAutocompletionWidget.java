package ua.com.fielden.platform.web.view.master.api.widgets.autocompleter.impl;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.EntityUtils;
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

    private final Map<String, Boolean> additionalProps = new LinkedHashMap<>();

    public AbstractEntityAutocompletionWidget(
            final String widgetPath,
            final Pair<String, String> titleAndDesc,
            final String propName,
            final Class<? extends AbstractEntity<?>> propType) {
        super(widgetPath, titleAndDesc, propName);

        // let's provide some sensible defaults for additional properties
        // in most cases description is included, but not searched by
        // whereas, in case of composite entities, all key members should be included and highlighted as they'are searched by
        additionalProps.put(AbstractEntity.DESC, false);
        if (EntityUtils.isCompositeEntity(propType)) {
            final List<Field> members = Finder.getKeyMembers(propType);
            for (final Field member: members) {
                additionalProps.put(member.getName(), true);
            }
        }
    }

    @Override
    protected Map<String, Object> createCustomAttributes() {
        final Map<String, Object> attrs = new LinkedHashMap<>();

        // let's collect the value for attribute additional-properties as JSON...
        final String additionalProperties = additionalProps.entrySet().stream()
                                      .map(entry -> format("\"%s\": %s", entry.getKey(), entry.getValue()))
                                      .collect(joining(",")).trim();
        // ...and if it's not empty then associated it with additional-properties
        if (!StringUtils.isEmpty(additionalProperties)) {
            attrs.put("additional-properties", format("{%s}", additionalProperties));
        }

        // assign other attributes...
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
        additionalProps.put(AbstractEntity.DESC, shouldSearchByDesc);
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
