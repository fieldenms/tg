package ua.com.fielden.platform.web.view.master.api.widgets.autocompleter.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

/**
 *
 * A wrapper for <code>tg-entity-editor</code> that represents a widget for specifying values of entity typed properties.
 *
 * @author TG Team
 *
 */
public class EntityAutocompletionWidget extends AbstractWidget {

    @SuppressWarnings("rawtypes")
    private Class<? extends IValueMatcher> matcherType;
    private boolean shouldSearchByDesc = false;
    private boolean shouldSearchByDescOnly = false;

    public EntityAutocompletionWidget(final Class<? extends AbstractEntity<?>> entityType, final String propertyName) {
        super("editors/tg-entity-editor", entityType, propertyName);
    }

    @Override
    protected Map<String, Object> createCustomAttributes() {
        final Map<String, Object> attrs = new LinkedHashMap<>();
        attrs.put("createModifiedPropertiesHolder", "{{createModifiedPropertiesHolder}}");
        attrs.put("user", "{{user}}");
        attrs.put("entitytype", "{{entitytype}}");
        return attrs;
    };

    @SuppressWarnings("rawtypes")
    public EntityAutocompletionWidget setMatcherType(final Class<? extends IValueMatcher> matcherType) {
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

    public boolean isShouldSearchByDescOnly() {
        return shouldSearchByDescOnly;
    }

    public void setShouldSearchByDescOnly(final boolean shouldSearchByDescOnly) {
        this.shouldSearchByDescOnly = shouldSearchByDescOnly;
    }

}
