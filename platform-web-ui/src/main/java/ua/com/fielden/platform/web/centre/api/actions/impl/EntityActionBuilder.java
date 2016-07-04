package ua.com.fielden.platform.web.centre.api.actions.impl;

import org.apache.commons.lang.StringUtils;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.web.PrefDim;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.actions.IEntityActionBuilder;
import ua.com.fielden.platform.web.centre.api.actions.IEntityActionBuilder0;
import ua.com.fielden.platform.web.centre.api.actions.IEntityActionBuilder0WithViews;
import ua.com.fielden.platform.web.centre.api.actions.IEntityActionBuilder1;
import ua.com.fielden.platform.web.centre.api.actions.IEntityActionBuilder2;
import ua.com.fielden.platform.web.centre.api.actions.IEntityActionBuilder3;
import ua.com.fielden.platform.web.centre.api.actions.IEntityActionBuilder4;
import ua.com.fielden.platform.web.centre.api.actions.IEntityActionBuilder5;
import ua.com.fielden.platform.web.centre.api.actions.IEntityActionBuilder6;
import ua.com.fielden.platform.web.centre.api.actions.IEntityActionBuilder7;
import ua.com.fielden.platform.web.centre.api.actions.IEntityActionBuilder7a;
import ua.com.fielden.platform.web.centre.api.actions.IEntityActionBuilder8;
import ua.com.fielden.platform.web.centre.api.actions.IEntityActionBuilder9;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;
import ua.com.fielden.platform.web.view.master.api.compound.Compound;

public class EntityActionBuilder<T extends AbstractEntity<?>> implements IEntityActionBuilder<T>, IEntityActionBuilder0<T>, IEntityActionBuilder0WithViews<T>, IEntityActionBuilder1<T>, IEntityActionBuilder2<T>, IEntityActionBuilder3<T>, IEntityActionBuilder4<T>, IEntityActionBuilder5<T>, IEntityActionBuilder6<T>, IEntityActionBuilder7<T> {
    private Injector injector;
    private IWebUiBuilder builder;
    private Class<? extends AbstractFunctionalEntityWithCentreContext<?>> functionalEntity;
    private CentreContextConfig context;
    private String icon;
    private String shortDesc;
    private String longDesc;
    private String shortcut;
    private IPreAction preAciton;
    private IPostAction successPostAction;
    private IPostAction errorPostAction;
    private PrefDim prefDimForView;
    private boolean returnNoAction;
    private boolean shouldRefreshParentCentreAfterSave = true;

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
     * A starting point to entity action configuration.
     *
     * @param functionalEntity
     * @return
     */
    public static <T extends AbstractEntity<?>> IEntityActionBuilder0WithViews<T> action(final Class<? extends AbstractFunctionalEntityWithCentreContext<?>> functionalEntity, final Injector injector, final IWebUiBuilder builder) {
        final EntityActionBuilder<T> actionBuilder = new EntityActionBuilder<T>();
        actionBuilder.injector = injector;
        actionBuilder.builder = builder;
        return actionBuilder.addAction(functionalEntity);
    }

    /**
     * Constructs entity action configuration that indicates the need to remove the default action if any.
     *
     * @return
     */
    public static <T extends AbstractFunctionalEntityWithCentreContext<?>> IEntityActionBuilder7a<T> actionOff() {
        return new EntityActionBuilder<T>().noAction();
    }

    private EntityActionBuilder() {
    }

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
                    shortcut,
                    preAciton,
                    successPostAction,
                    errorPostAction,
                    prefDimForView,
                    shouldRefreshParentCentreAfterSave);
        }
    }

    @Override
    public IEntityActionBuilder0WithViews<T> addAction(final Class<? extends AbstractFunctionalEntityWithCentreContext<?>> functionalEntity) {
        if (functionalEntity == null) {
            throw new IllegalArgumentException("Functional entity type should be provided.");
        }

        this.functionalEntity = functionalEntity;
        return this;
    }

    @Override
    public IEntityActionBuilder3<T> postActionSuccess(final IPostAction postAction) {
        if (postAction == null) {
            throw new IllegalArgumentException("Post successful action should be provided.");
        }

        this.successPostAction = postAction;
        return this;
    }

    @Override
    public IEntityActionBuilder4<T> postActionError(final IPostAction postAction) {
        if (postAction == null) {
            throw new IllegalArgumentException("Post error action should be provided.");
        }

        this.errorPostAction = postAction;
        return this;
    }

    @Override
    public IEntityActionBuilder5<T> icon(final String iconName) {
        if (StringUtils.isEmpty(iconName)) {
            throw new IllegalArgumentException("Icon name should be provided.");
        }

        this.icon = iconName;
        return this;
    }

    @Override
    public IEntityActionBuilder6<T> shortDesc(final String shortDesc) {
        if (StringUtils.isEmpty(shortDesc)) {
            throw new IllegalArgumentException("Short description should be provided.");
        }

        this.shortDesc = shortDesc;
        return this;
    }

    @Override
    public IEntityActionBuilder2<T> preAction(final IPreAction preAction) {
        if (preAction == null) {
            throw new IllegalArgumentException("Pre action should be provided.");
        }

        this.preAciton = preAction;
        return this;
    }

    @Override
    public IEntityActionBuilder1<T> withContext(final CentreContextConfig contextConfig) {
        if (contextConfig == null) {
            throw new IllegalArgumentException("Context configuration should be provided.");
        }

        this.context = contextConfig;
        return this;
    }

    @Override
    public IEntityActionBuilder7<T> longDesc(final String longDesc) {
        if (StringUtils.isEmpty(longDesc)) {
            throw new IllegalArgumentException("Long description should be provided.");
        }

        this.longDesc = longDesc;
        return this;
    }
    
    @Override
    public IEntityActionBuilder7a<T> shortcut(final String shortcut) {
        if (StringUtils.isEmpty(shortcut)) {
            throw new IllegalArgumentException("Shortcut should be provided.");
        }

        this.shortcut = shortcut;
        return this;
    }

    @Override
    public IEntityActionBuilder7a<T> noAction() {
        this.returnNoAction = true;
        return this;
    }

	@Override
	public IEntityActionBuilder8<T> prefDimForView(final PrefDim dim) {
		this.prefDimForView = dim;
		return this;
	}

	@Override
	public IEntityActionBuilder9<T> withNoParentCentreRefresh() {
		this.shouldRefreshParentCentreAfterSave = false;
		return this;
	}

    @Override
    public IEntityActionBuilder0<T> withView(final EntityCentre<?> embeddedCentre) {
        builder.register(Compound.detailsCentre(functionalEntity, builder.register(embeddedCentre), injector));
        return this;
    }
    
    @Override
    public IEntityActionBuilder0<T> withView(final EntityMaster<?> embeddedMaster) {
        builder.register(Compound.detailsMaster(functionalEntity, builder.register(embeddedMaster), injector));
        return this;
    }
}
