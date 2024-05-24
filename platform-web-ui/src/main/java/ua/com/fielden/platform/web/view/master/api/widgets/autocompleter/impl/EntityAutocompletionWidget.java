package ua.com.fielden.platform.web.view.master.api.widgets.autocompleter.impl;

import static ua.com.fielden.platform.web.centre.WebApiUtils.webComponent;

import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.Pair;

/**
 *
 * A wrapper for <code>tg-entity-editor</code> that represents a widget for specifying values of entity typed properties on Entity Master.
 *
 * @author TG Team
 *
 */
public class EntityAutocompletionWidget extends AbstractEntityAutocompletionWidget {

    public EntityAutocompletionWidget(final Pair<String, String> titleAndDesc, final String propertyName, final Class<? extends AbstractEntity<?>> propertyType) {
        super(webComponent("editors/tg-entity-editor"), titleAndDesc, propertyName, propertyType);
    }

    @Override
    protected Map<String, Object> createCustomAttributes() {
        final Map<String, Object> attrs = super.createCustomAttributes();

        attrs.put("autocompletion-type", "[[entityType]]");

        attrs.put("as-part-of-entity-master", true);
        attrs.put("create-modified-properties-holder", "[[_createModifiedPropertiesHolder]]");
        attrs.put("originally-produced-entity", "[[_originallyProducedEntity]]");
        attrs.put("last-validation-attempt-promise", "[[lastValidationAttemptPromise]]");
        attrs.put("tg-open-master-action", "[[titleAction]]");
        return attrs;
    };

}
