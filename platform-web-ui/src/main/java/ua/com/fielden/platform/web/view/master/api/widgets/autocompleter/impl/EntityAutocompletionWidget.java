package ua.com.fielden.platform.web.view.master.api.widgets.autocompleter.impl;

import java.util.Map;

import ua.com.fielden.platform.utils.Pair;

/**
 *
 * A wrapper for <code>tg-entity-editor</code> that represents a widget for specifying values of entity typed properties on Entity Master.
 *
 * @author TG Team
 *
 */
public class EntityAutocompletionWidget extends AbstractEntityAutocompletionWidget {
    private boolean shouldSearchByDescOnly = false;

    public EntityAutocompletionWidget(final Pair<String, String> titleDesc, final String propertyName, final boolean selectionCriteriaWidget) {
        super("editors/tg-entity-editor", titleDesc, propertyName, null, selectionCriteriaWidget);
    }

    @Override
    protected Map<String, Object> createCustomAttributes() {
        final Map<String, Object> attrs = super.createCustomAttributes();
        // attrs.put("hightlightDesc", Boolean.toString(shouldSearchByDesc));
        attrs.put("asPartOfEntityMaster", true);
        attrs.put("createModifiedPropertiesHolder", "{{createModifiedPropertiesHolder}}");
        return attrs;
    };

    public boolean isShouldSearchByDescOnly() {
        return shouldSearchByDescOnly;
    }

    public void setShouldSearchByDescOnly(final boolean shouldSearchByDescOnly) {
        this.shouldSearchByDescOnly = shouldSearchByDescOnly;
    }

}
