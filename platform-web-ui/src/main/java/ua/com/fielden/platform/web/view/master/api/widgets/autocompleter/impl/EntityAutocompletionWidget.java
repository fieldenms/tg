package ua.com.fielden.platform.web.view.master.api.widgets.autocompleter.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.Pair;

import java.util.Map;

/// A wrapper for `tg-entity-editor` that represents a widget for specifying values of entity typed properties on Entity Master.
///
public class EntityAutocompletionWidget extends AbstractEntityAutocompletionWidget {

    private final boolean multi;

    public EntityAutocompletionWidget(final Pair<String, String> titleAndDesc, final String propertyName, final Class<? extends AbstractEntity<?>> propertyType, final boolean multi) {
        super("editors/tg-entity-editor", titleAndDesc, propertyName, propertyType);
        this.multi = multi;
    }

    @Override
    protected Map<String, Object> createCustomAttributes() {
        final Map<String, Object> attrs = super.createCustomAttributes();

        attrs.put("autocompletion-type", "[[entityType]]");

        attrs.put("multi", this.multi);
        attrs.put("as-part-of-entity-master", true);
        attrs.put("create-modified-properties-holder", "[[_createModifiedPropertiesHolder]]");
        attrs.put("originally-produced-entity", "[[_originallyProducedEntity]]");
        attrs.put("tg-open-master-action", "[[titleAction]]");
        return attrs;
    };

}
