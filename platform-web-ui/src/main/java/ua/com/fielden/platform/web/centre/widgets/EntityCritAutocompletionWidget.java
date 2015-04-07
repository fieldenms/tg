package ua.com.fielden.platform.web.centre.widgets;

import java.util.LinkedHashMap;
import java.util.Map;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

/**
 *
 * A wrapper for <code>tg-entity-search-criteria</code> that represents a widget for specifying multiple search queries against a property of an entity type as a part of an entity
 * centre.
 *
 *
 * @author TG Team
 *
 */
public class EntityCritAutocompletionWidget extends AbstractWidget {

    @SuppressWarnings("rawtypes")
    private Class<? extends IValueMatcher> matcherType;
    private boolean shouldSearchByDesc = false;

    public EntityCritAutocompletionWidget(final Pair<String, String> titleDesc, final String propertyName) {
        super("editors/tg-entity-search-criteria", titleDesc, propertyName);
    }

    @Override
    protected Map<String, Object> createCustomAttributes() {
        final Map<String, Object> attrs = new LinkedHashMap<>();
        attrs.put("createModifiedPropertiesHolder", "{{createModifiedPropertiesHolder}}");
        attrs.put("user", "{{user}}");
        attrs.put("entitytype", "{{entitytype}}");
        attrs.put("hightlightDesc", Boolean.toString(shouldSearchByDesc));
        attrs.put("getSelectedEntities", "{{getSelectedEntities}}");
        attrs.put("requireSelectedEntities", "ALL"); // TODO
        return attrs;
    };

    @SuppressWarnings("rawtypes")
    public EntityCritAutocompletionWidget setMatcherType(final Class<? extends IValueMatcher> matcherType) {
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
