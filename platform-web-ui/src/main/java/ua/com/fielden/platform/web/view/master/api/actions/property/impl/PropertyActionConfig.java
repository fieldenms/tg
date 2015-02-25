package ua.com.fielden.platform.web.view.master.api.actions.property.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.actions.EnabledState;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;
import ua.com.fielden.platform.web.view.master.api.actions.property.IPropertyActionConfig0;
import ua.com.fielden.platform.web.view.master.api.actions.property.IPropertyActionConfig1;
import ua.com.fielden.platform.web.view.master.api.actions.property.IPropertyActionConfig2;
import ua.com.fielden.platform.web.view.master.api.actions.property.IPropertyActionConfig3;
import ua.com.fielden.platform.web.view.master.api.actions.property.IPropertyActionConfig4;
import ua.com.fielden.platform.web.view.master.api.actions.property.IPropertyActionConfig5;
import ua.com.fielden.platform.web.view.master.api.actions.property.IPropertyActionConfig6;
import ua.com.fielden.platform.web.view.master.api.actions.property.IPropertyActionConfig7;
import ua.com.fielden.platform.web.view.master.api.helpers.IPropertySelector;

public class PropertyActionConfig<T extends AbstractEntity<?>> implements IPropertyActionConfig0<T>, IPropertyActionConfig1<T>, IPropertyActionConfig2<T>, IPropertyActionConfig3<T>, IPropertyActionConfig4<T>, IPropertyActionConfig5<T>, IPropertyActionConfig6<T>, IPropertyActionConfig7<T> {

    private final PropertyAction action;
    private final IPropertySelector<T> propSelector;

    public PropertyActionConfig(final PropertyAction action, final IPropertySelector<T> propSelector) {
        this.action = action;
        this.propSelector = propSelector;
    }

    @Override
    public IPropertySelector<T> also() {
        return propSelector;
    }

    @Override
    public IPropertyActionConfig7<T> longDesc(final String longDesc) {
        action.setLongDesc(longDesc);
        return this;
    }

    @Override
    public IPropertyActionConfig6<T> shortDesc(final String shortDesc) {
        action.setShortDesc(shortDesc);
        return this;
    }

    @Override
    public IPropertyActionConfig5<T> icon(final String iconName) {
        action.setIcon(iconName);
        return this;
    }

    @Override
    public IPropertyActionConfig4<T> enabledWhen(final EnabledState state) {
        action.setEnabledState(state);
        return this;
    }

    @Override
    public IPropertyActionConfig2<T> postActionSuccess(final IPostAction postActionSuccess) {
        action.setPostActionSuccess(postActionSuccess);
        return this;
    }

    @Override
    public IPropertyActionConfig3<T> postActionError(final IPostAction postActionError) {
        action.setPostActionError(postActionError);
        return this;
    }

    @Override
    public IPropertyActionConfig1<T> preAction(final IPreAction preAction) {
        action.setPreAction(preAction);
        return this;
    }

}
