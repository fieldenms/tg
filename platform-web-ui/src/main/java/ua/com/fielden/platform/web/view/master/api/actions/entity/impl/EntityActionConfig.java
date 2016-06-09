package ua.com.fielden.platform.web.view.master.api.actions.entity.impl;

import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.ILayout.Orientation;
import ua.com.fielden.platform.web.view.master.api.actions.EnabledState;
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.actions.entity.IEntityActionConfig0;
import ua.com.fielden.platform.web.view.master.api.actions.entity.IEntityActionConfig1;
import ua.com.fielden.platform.web.view.master.api.actions.entity.IEntityActionConfig2;
import ua.com.fielden.platform.web.view.master.api.actions.entity.IEntityActionConfig3;
import ua.com.fielden.platform.web.view.master.api.actions.entity.IEntityActionConfig4;
import ua.com.fielden.platform.web.view.master.api.actions.entity.IEntityActionConfig5;
import ua.com.fielden.platform.web.view.master.api.actions.entity.IEntityActionConfig6;
import ua.com.fielden.platform.web.view.master.api.actions.entity.IEntityActionConfig7;
import ua.com.fielden.platform.web.view.master.api.actions.impl.AbstractAction;
import ua.com.fielden.platform.web.view.master.api.actions.impl.AbstractFunctionalAction;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;
import ua.com.fielden.platform.web.view.master.api.helpers.ILayoutConfigWithDone;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;

public class EntityActionConfig<T extends AbstractEntity<?>> implements IEntityActionConfig0<T>, IEntityActionConfig1<T>, IEntityActionConfig2<T>, IEntityActionConfig3<T>, IEntityActionConfig4<T>, IEntityActionConfig5<T>, IEntityActionConfig6<T>, IEntityActionConfig7<T> {

    private final AbstractAction action;
    private final SimpleMasterBuilder<T> simpleMasterBuilder;

    public EntityActionConfig(final AbstractAction action, final SimpleMasterBuilder<T> simpleMaster) {
        this.action = action;
        this.simpleMasterBuilder = simpleMaster;
    }

    @Override
    public IEntityActionConfig0<T> addAction(final MasterActions masterAction) {
        return simpleMasterBuilder.addAction(masterAction);
    }

    @Override
    public ILayoutConfigWithDone<T> setLayoutFor(final Device device, final Optional<Orientation> orientation, final String flexString) {
        return simpleMasterBuilder.setLayoutFor(device, orientation, flexString);
    }

    @Override
    public IEntityActionConfig7<T> longDesc(final String longDesc) {
        action.setLongDesc(longDesc);
        return this;
    }

    @Override
    public IEntityActionConfig6<T> shortDesc(final String shortDesc) {
        action.setShortDesc(shortDesc);
        return this;
    }

    @Override
    public IEntityActionConfig5<T> icon(final String iconName) {
        action.setIcon(iconName);
        return this;
    }

    @Override
    public IEntityActionConfig4<T> enabledWhen(final EnabledState state) {
        action.setEnabledState(state);
        return this;
    }

    @Override
    public IEntityActionConfig2<T> postActionSuccess(final IPostAction postActionSuccess) {
        ((AbstractFunctionalAction) action).setPostActionSuccess(postActionSuccess);
        return this;
    }

    @Override
    public IEntityActionConfig3<T> postActionError(final IPostAction postActionError) {
        ((AbstractFunctionalAction) action).setPostActionError(postActionError);
        return this;
    }

    @Override
    public IEntityActionConfig1<T> preAction(final IPreAction preAction) {
        ((AbstractFunctionalAction) action).setPreAction(preAction);
        return this;
    }

    public AbstractAction action() {
        return action;
    }

    @Override
    public IEntityActionConfig7<T> addAction(final ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig actionConfig) {
        return simpleMasterBuilder.addAction(actionConfig);
    }
}
