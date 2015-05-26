package ua.com.fielden.platform.web.centre.api.crit.impl;

import java.util.Map;

import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.autocompleter.impl.AbstractEntityAutocompletionWidget;

/**
 *
 * A wrapper for <code>tg-entity-editor</code> that represents a widget for specifying values in single entity-typed criteria on Entity Centre.
 *
 * @author TG Team
 *
 */
public class EntitySingleCritAutocompletionWidget extends AbstractEntityAutocompletionWidget {
    private boolean shouldSearchByDescOnly = false;

    public EntitySingleCritAutocompletionWidget(final Pair<String, String> titleDesc, final String propertyName, final CentreContextConfig centreContextConfig, final boolean selectionCriteriaWidget) {
        super("editors/tg-entity-editor", titleDesc, propertyName, centreContextConfig, selectionCriteriaWidget);
    }

    @Override
    protected Map<String, Object> createCustomAttributes() {
        final Map<String, Object> attrs = super.createCustomAttributes();
        attrs.put("asPartOfEntityMaster", false);
        // attrs.put("hightlightDesc", Boolean.toString(shouldSearchByDesc));
        return attrs;
    };

    public boolean isShouldSearchByDescOnly() {
        return shouldSearchByDescOnly;
    }

    public void setShouldSearchByDescOnly(final boolean shouldSearchByDescOnly) {
        this.shouldSearchByDescOnly = shouldSearchByDescOnly;
    }

}
