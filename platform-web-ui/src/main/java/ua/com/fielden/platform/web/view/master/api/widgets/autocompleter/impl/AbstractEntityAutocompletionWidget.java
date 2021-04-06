package ua.com.fielden.platform.web.view.master.api.widgets.autocompleter.impl;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.entity.AbstractEntity.DESC;
import static ua.com.fielden.platform.reflection.Finder.getKeyMembers;
import static ua.com.fielden.platform.utils.EntityUtils.hasDescProperty;
import static ua.com.fielden.platform.utils.EntityUtils.isCompositeEntity;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.Pair;
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
    private boolean lightDesc = false;

    private final Map<String, Boolean> additionalProps = new LinkedHashMap<>();
    private final Map<String, Boolean> defaultAdditionalProps = new LinkedHashMap<>();

    protected AbstractEntityAutocompletionWidget(
            final String widgetPath,
            final Pair<String, String> titleAndDesc,
            final String propName,
            final Class<? extends AbstractEntity<?>> propType) {
        super(widgetPath, titleAndDesc, propName);
        
        defaultAdditionalProps.putAll(createDefaultAdditionalProps(propType));
        
        // assigned the collected default props 
        additionalProps.putAll(defaultAdditionalProps);
    }
    
    /**
     * Creates default additional properties (except key) for autocompleter. This includes description if the type contains it.
     * Also this includes key members if the type is composite.
     * 
     * @param propType
     * @return
     */
    public static Map<String, Boolean> createDefaultAdditionalProps(final Class<? extends AbstractEntity<?>> propType) {
        final Map<String, Boolean> defaultAdditionalProps = new LinkedHashMap<>();
        // let's provide some sensible defaults for additional properties
        // in most cases description is included if it exists for the type... also it is searched by default
        if (hasDescProperty(propType)) {
            defaultAdditionalProps.put(DESC, true);
        }
        // in case of composite entities that has more than one key member, all key members should be included and highlighted as they'are searched by
        // in case of a single key member, displaying only the key is sufficient
        if (isCompositeEntity(propType)) {
            final List<Field> members = getKeyMembers(propType);
            if (members.size() > 1) {
                for (final Field member: members) {
                    defaultAdditionalProps.put(member.getName(), true);
                }
            }
        }
        return defaultAdditionalProps;
    }

    public AbstractEntityAutocompletionWidget setAdditionalProps(final List<Pair<String, Boolean>> pairs) {
        additionalProps.clear();
        for (final Pair<String, Boolean> pair: pairs) {
            // TODO potentially there could be a check whether the specified properties really belong to a corresponding entity type
            additionalProps.put(pair.getKey(), pair.getValue());
        }
        if (additionalProps.isEmpty()) {
            additionalProps.putAll(defaultAdditionalProps);
        }
        return this;
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
    }

    @SuppressWarnings("rawtypes")
    public AbstractEntityAutocompletionWidget setMatcherType(final Class<? extends IValueMatcher> matcherType) {
        this.matcherType = matcherType;
        return this;
    }

    @SuppressWarnings("rawtypes")
    public Class<? extends IValueMatcher> getMatcherType() {
        return matcherType;
    }

    public boolean isLightDesc() {
        return lightDesc;
    }

    public void setLightDesc(final boolean shouldSearchByDesc) {
        additionalProps.put(AbstractEntity.DESC, shouldSearchByDesc);
        this.lightDesc = shouldSearchByDesc;
    }
    
    /**
     * Additional properties (except key) for autocompleter. This includes description if the type contains it.
     * Also this includes key members if the type is composite.
     * 
     * @return
     */
    public Map<String, Boolean> additionalProps() {
        return unmodifiableMap(additionalProps);
    }
    
}