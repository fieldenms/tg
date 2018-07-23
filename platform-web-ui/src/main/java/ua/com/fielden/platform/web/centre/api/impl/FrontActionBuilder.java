package ua.com.fielden.platform.web.centre.api.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.front_actions.IAlsoFrontActions;
import ua.com.fielden.platform.web.centre.api.front_actions.IFrontActions;
import ua.com.fielden.platform.web.centre.api.top_level_actions.IAlsoCentreTopLevelActions;
import ua.com.fielden.platform.web.centre.api.top_level_actions.ICentreTopLevelActionsInGroup;

public class FrontActionBuilder<T extends AbstractEntity<?>> implements IFrontActions<T>, IAlsoFrontActions<T> {

    private final EntityCentreBuilder<T> builder;

    public FrontActionBuilder(final EntityCentreBuilder<T> builder) {
        this.builder = builder;
    }

    @Override
    public IAlsoFrontActions<T> addFrontAction(final EntityActionConfig actionConfig) {
        builder.frontActions.add(actionConfig);
        return this;
    }

    @Override
    public IFrontActions<T> also() {
        return this;
    }

    @Override
    public IAlsoCentreTopLevelActions<T> addTopAction(final EntityActionConfig actionConfig) {
        return new TopLevelActionsBuilder<>(builder).addTopAction(actionConfig);
    }

    @Override
    public ICentreTopLevelActionsInGroup<T> beginTopActionsGroup(final String desc) {
        return new TopLevelActionsBuilder<>(builder).beginTopActionsGroup(desc);
    };
}
