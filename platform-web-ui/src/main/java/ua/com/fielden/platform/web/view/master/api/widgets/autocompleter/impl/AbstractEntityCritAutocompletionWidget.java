package ua.com.fielden.platform.web.view.master.api.widgets.autocompleter.impl;

import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.crit.impl.EntitySingleCritAutocompletionWidget;
import ua.com.fielden.platform.web.centre.widgets.EntityMultiCritAutocompletionWidget;

/**
 *
 * A base class for selection criteria as autocompleters.
 *
 * @author TG Team
 *
 */
public abstract class AbstractEntityCritAutocompletionWidget extends AbstractEntityAutocompletionWidget {

    protected AbstractEntityCritAutocompletionWidget(
            final String widgetPath,
            final Pair<String, String> titleAndDesc,
            final String propName,
            final Class<? extends AbstractEntity<?>> propType) {
        super(widgetPath, titleAndDesc, propName, propType);
    }


    /**
     * Adds the bindings for centre context (if it is not empty).
     *
     * Applicable only for {@link EntityMultiCritAutocompletionWidget} and {@link EntitySingleCritAutocompletionWidget}.
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
