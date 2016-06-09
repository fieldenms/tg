package ua.com.fielden.platform.web.centre.api.crit.impl;

import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.autocompleter.impl.AbstractEntityCritAutocompletionWidget;

/**
 *
 * A wrapper for <code>tg-entity-editor</code> that represents a widget for specifying values in single entity-typed criteria on Entity Centre.
 *
 * @author TG Team
 *
 */
public class EntitySingleCritAutocompletionWidget extends AbstractEntityCritAutocompletionWidget {
    private boolean shouldSearchByDescOnly = false;
    private final CentreContextConfig centreContextConfig;

    public EntitySingleCritAutocompletionWidget(final Pair<String, String> titleAndDesc, final String propertyName, final Class<? extends AbstractEntity<?>> propType, final CentreContextConfig centreContextConfig) {
        super("editors/tg-entity-editor", titleAndDesc, propertyName, propType);
        this.centreContextConfig = centreContextConfig;
    }

    @Override
    protected Map<String, Object> createCustomAttributes() {
        final Map<String, Object> attrs = super.createCustomAttributes();
        attrs.put("autocompletion-type", "[[miType]]");

        attrs.put("as-part-of-entity-master", false);
        addCentreContextBindings(attrs, centreContextConfig);
        return attrs;
    };

    public boolean isShouldSearchByDescOnly() {
        return shouldSearchByDescOnly;
    }

    public void setShouldSearchByDescOnly(final boolean shouldSearchByDescOnly) {
        this.shouldSearchByDescOnly = shouldSearchByDescOnly;
    }

}
