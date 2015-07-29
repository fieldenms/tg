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

    public EntityAutocompletionWidget(final Pair<String, String> titleDesc, final String propertyName) {
        super("editors/tg-entity-editor", titleDesc, propertyName);
    }

    @Override
    protected Map<String, Object> createCustomAttributes() {
        final Map<String, Object> attrs = super.createCustomAttributes();

        attrs.put("autocompletion-type", "[[entityType]]");

        attrs.put("as-part-of-entity-master", true);
        attrs.put("create-modified-properties-holder", "[[_createModifiedPropertiesHolder]]");
        return attrs;
    };

    public boolean isShouldSearchByDescOnly() {
        return shouldSearchByDescOnly;
    }

    public void setShouldSearchByDescOnly(final boolean shouldSearchByDescOnly) {
        this.shouldSearchByDescOnly = shouldSearchByDescOnly;
    }

}
