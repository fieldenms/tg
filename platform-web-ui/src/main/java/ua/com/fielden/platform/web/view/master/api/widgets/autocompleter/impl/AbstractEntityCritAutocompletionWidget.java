package ua.com.fielden.platform.web.view.master.api.widgets.autocompleter.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.widgets.EntityMultiCritAutocompletionWidget;
import ua.com.fielden.platform.web.centre.widgets.EntitySingleCritAutocompletionWidget;

import java.util.Map;

import static ua.com.fielden.platform.utils.EntityUtils.isActivatableEntityType;

/// A base class for selection criteria as autocompleters.
///
public abstract class AbstractEntityCritAutocompletionWidget extends AbstractEntityAutocompletionWidget {

    protected AbstractEntityCritAutocompletionWidget(
            final String widgetPath,
            final Pair<String, String> titleAndDesc,
            final String propName,
            final Class<? extends AbstractEntity<?>> propType) {
        super(widgetPath, titleAndDesc, propName, propType);
    }

    @Override
    protected Map<String, Object> createCustomAttributes() {
        final Map<String, Object> attrs = super.createCustomAttributes();
        if (isActivatableEntityType(propType)) {
            attrs.put("_update-centre-dirty", "[[_updateCentreDirty]]");
        }
        return attrs;
    }

    /// Adds the bindings for centre context (if it is not empty).
    ///
    /// Applicable only for [EntityMultiCritAutocompletionWidget] and [EntitySingleCritAutocompletionWidget].
    ///
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