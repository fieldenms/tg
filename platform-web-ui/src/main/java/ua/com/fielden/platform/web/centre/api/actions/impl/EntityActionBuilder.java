package ua.com.fielden.platform.web.centre.api.actions.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.actions.IEntityActionBuilder;
import ua.com.fielden.platform.web.centre.api.actions.IEntityActionBuilder0;
import ua.com.fielden.platform.web.centre.api.actions.IEntityActionBuilder1;
import ua.com.fielden.platform.web.centre.api.actions.IEntityActionBuilder2;
import ua.com.fielden.platform.web.centre.api.actions.IEntityActionBuilder3;
import ua.com.fielden.platform.web.centre.api.actions.IEntityActionBuilder4;
import ua.com.fielden.platform.web.centre.api.actions.IEntityActionBuilder5;
import ua.com.fielden.platform.web.centre.api.actions.IEntityActionBuilder6;
import ua.com.fielden.platform.web.centre.api.actions.IEntityActionBuilder7;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;

public class EntityActionBuilder<T extends AbstractEntity<?>> implements IEntityActionBuilder<T>, IEntityActionBuilder0<T>, IEntityActionBuilder1<T>, IEntityActionBuilder2<T>, IEntityActionBuilder3<T>, IEntityActionBuilder4<T>, IEntityActionBuilder5<T>, IEntityActionBuilder6<T>, IEntityActionBuilder7<T> {

    private Class<? extends AbstractFunctionalEntityWithCentreContext<?>> functionalEntity;
    private CentreContextConfig context;
    private String icon;
    private String shortDesc;
    private String longDesc;
    private IPreAction preAciton;
    private IPostAction successPostAction;
    private IPostAction errorPostAction;
    private boolean returnNoAction;

    /**
     * A starting point to entity action configuration.
     *
     * @param functionalEntity
     * @return
     */
    public static <T extends AbstractEntity<?>> IEntityActionBuilder0<T> action(final Class<? extends AbstractFunctionalEntityWithCentreContext<?>> functionalEntity) {
        return new EntityActionBuilder<T>().addAction(functionalEntity);
    }

    /**
     * Constructs entity action configuration that indicates the need to remove the default action if any.
     *
     * @return
     */
    public static <T extends AbstractFunctionalEntityWithCentreContext<?>> IEntityActionBuilder7<T> actionOff() {
        return new EntityActionBuilder<T>().noAction();
    }

    private EntityActionBuilder() {}

    @Override
    public EntityActionConfig build() {
        if (returnNoAction) {
            return EntityActionConfig.createNoActionConfig();
        } else {
            return EntityActionConfig.createActionConfig(
                    functionalEntity,
                    context,
                    icon,
                    shortDesc,
                    longDesc,
                    preAciton,
                    successPostAction,
                    errorPostAction);
        }
    }

    @Override
    public IEntityActionBuilder0<T> addAction(final Class<? extends AbstractFunctionalEntityWithCentreContext<?>> functionalEntity) {
        this.functionalEntity = functionalEntity;
        return this;
    }

    @Override
    public IEntityActionBuilder3<T> postActionSuccess(final IPostAction postAction) {
        this.successPostAction = postAction;
        return this;
    }

    @Override
    public IEntityActionBuilder4<T> postActionError(final IPostAction postAction) {
        this.errorPostAction = postAction;
        return this;
    }

    @Override
    public IEntityActionBuilder5<T> icon(final String iconName) {
        this.icon = iconName;
        return this;
    }

    @Override
    public IEntityActionBuilder6<T> shortDesc(final String shortDesc) {
        this.shortDesc = shortDesc;
        return this;
    }

    @Override
    public IEntityActionBuilder2<T> preAction(final IPreAction preAction) {
        this.preAciton = preAction;
        return this;
    }

    @Override
    public IEntityActionBuilder1<T> withContext(final CentreContextConfig contextConfig) {
        this.context = contextConfig;
        return this;
    }

    @Override
    public IEntityActionBuilder7<T> longDesc(final String longDesc) {
        this.longDesc = longDesc;
        return this;
    }

    @Override
    public IEntityActionBuilder7<T> noAction() {
        this.returnNoAction = true;
        return this;
    }

}
