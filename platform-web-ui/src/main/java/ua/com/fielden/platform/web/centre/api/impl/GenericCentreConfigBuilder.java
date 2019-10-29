package ua.com.fielden.platform.web.centre.api.impl;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.crit.ISelectionCritKindSelector;
import ua.com.fielden.platform.web.centre.api.front_actions.IAlsoFrontActions;
import ua.com.fielden.platform.web.centre.api.front_actions.IFrontWithTopActions;
import ua.com.fielden.platform.web.centre.api.top_level_actions.IAlsoCentreTopLevelActions;
import ua.com.fielden.platform.web.centre.api.top_level_actions.ICentreTopLevelActionsInGroup;
import ua.com.fielden.platform.web.centre.api.top_level_actions.ICentreTopLevelActionsWithEnforcePostSaveRefreshConfig;
import ua.com.fielden.platform.web.centre.api.top_level_actions.ICentreTopLevelActionsWithRunConfig;
import ua.com.fielden.platform.web.centre.api.top_level_actions.ICentreTopLevelActionsWithSse;

public class GenericCentreConfigBuilder<T extends AbstractEntity<?>> extends ResultSetBuilder<T> implements ICentreTopLevelActionsWithRunConfig<T>{

    private final EntityCentreBuilder<T> builder;

    public GenericCentreConfigBuilder(final EntityCentreBuilder<T> builder) {
        super(builder);
        this.builder = builder;
    }

    @Override
    public ICentreTopLevelActionsWithEnforcePostSaveRefreshConfig<T> hasEventSourceAt(final String uri) {
        if (StringUtils.isEmpty(uri)) {
            throw new IllegalArgumentException("Server-Side Eventing URI should not be empty.");
        }
        builder.sseUri = uri;
        return this;
    }

    @Override
    public IFrontWithTopActions<T> enforcePostSaveRefresh() {
        builder.enforcePostSaveRefresh = true;
        return this;
    }

    @Override
    public IAlsoFrontActions<T> addFrontAction(final EntityActionConfig actionConfig) {
        return new FrontActionBuilder<>(builder).addFrontAction(actionConfig);
    }

    @Override
    public IAlsoCentreTopLevelActions<T> addTopAction(final EntityActionConfig actionConfig) {
        return new TopLevelActionsBuilder<>(builder).addTopAction(actionConfig);
    }

    @Override
    public ICentreTopLevelActionsInGroup<T> beginTopActionsGroup(final String desc) {
        return new TopLevelActionsBuilder<>(builder).beginTopActionsGroup(desc);
    }

    @Override
    public ISelectionCritKindSelector<T> addCrit(final String propName) {
        return new TopLevelActionsBuilder<T>(builder).addCrit(propName);
    }

    @Override
    public ICentreTopLevelActionsWithSse<T> runAutomatically() {
        builder.runAutomatically = true;
        return this;
    }
}
