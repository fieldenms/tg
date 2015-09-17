package ua.com.fielden.platform.web.centre.api.actions;

import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;

import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.sample.domain.MasterInDialogInvocationFunctionalEntity;
import ua.com.fielden.platform.sample.domain.MasterInvocationFunctionalEntity;
import ua.com.fielden.platform.sample.domain.ShowViewInDialogFunctionalEntity;
import ua.com.fielden.platform.web.PrefDim;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;

/**
 * Configuration of a specific entity action, which is associated with an entity on an entity centre.
 *
 * @author TG Team
 *
 */
public final class EntityActionConfig {
    public final Optional<Class<? extends AbstractFunctionalEntityWithCentreContext<?>>> functionalEntity;
    public final Optional<EntityCentre<?>> entityCentre;
    public final Optional<CentreContextConfig> context;
    public final Optional<String> icon;
    public final Optional<String> shortDesc;
    public final Optional<String> longDesc;
    public final Optional<IPreAction> preAction;
    public final Optional<IPostAction> successPostAction;
    public final Optional<IPostAction> errorPostAction;
    public final Optional<PrefDim> prefDimForView;
    private final boolean noAction;

    private EntityActionConfig(
            final Class<? extends AbstractFunctionalEntityWithCentreContext<?>> functionalEntity,
            final EntityCentre<?> entityCentre,
            final CentreContextConfig context,
            final String icon,
            final String shortDesc,
            final String longDesc,
            final IPreAction preAction,
            final IPostAction successPostAction,
            final IPostAction errorPostAction,
            final PrefDim prefDimForView,
            final boolean noAction) {

        if (!noAction && functionalEntity == null) {
            throw new IllegalArgumentException("A functional entity type should be provided.");
        }

        if (functionalEntity != null && context == null) {
            throw new IllegalArgumentException("Any functional entity requires some execution context to be specified.");
        }

        this.functionalEntity = Optional.ofNullable(functionalEntity);
        this.entityCentre = Optional.ofNullable(entityCentre);
        this.context = Optional.ofNullable(context);
        this.icon = Optional.ofNullable(icon);
        this.shortDesc = Optional.ofNullable(shortDesc);
        this.longDesc = Optional.ofNullable(longDesc);
        this.preAction = Optional.ofNullable(preAction);
        this.successPostAction = Optional.ofNullable(successPostAction);
        this.errorPostAction = Optional.ofNullable(errorPostAction);
        this.prefDimForView = Optional.ofNullable(prefDimForView);
        this.noAction = noAction;
    }

    /**
     * A factory method for creating a configuration that indicates a need to skip creation of any action.
     *
     * @return
     */
    public static EntityActionConfig createNoActionConfig() {
        return new EntityActionConfig(null, null, null, null, null, null, null, null, null, null, true);
    }

    /**
     * A factory method for creating a configuration that indicates a need to invoke corresponding master for row entity.
     *
     * @return
     */
    public static EntityActionConfig createMasterInvocationActionConfig() {
        return new EntityActionConfig(MasterInvocationFunctionalEntity.class, null, context().withCurrentEntity().build(), null, "Edit row entity", null, null, null, null, null, false);
    }

    /**
     * A factory method for creating a configuration that indicates a need to invoke corresponding master in dialog for row entity.
     *
     * @return
     */
    public static EntityActionConfig createMasterInDialogInvocationActionConfig() {
        return new EntityActionConfig(MasterInDialogInvocationFunctionalEntity.class, null, context().withCurrentEntity().build(), null, "Edit row entity", null, null, null, null, null, false);
    }
    
    /**
     * Same as above, but with preferred dimensions.
     *  
     * @param dim
     * @return
     */
    public static EntityActionConfig createMasterInDialogInvocationActionConfig(final PrefDim dim) {
        return new EntityActionConfig(MasterInDialogInvocationFunctionalEntity.class, null, context().withCurrentEntity().build(), null, "Edit row entity", null, null, null, null, dim, false);
    }

    /**
     * A factory method for creating a configuration that indicates a need to invoke corresponding entity centre in dialog.
     *
     * @return
     */
    public static EntityActionConfig createShowViewInDialogAction(final EntityCentre<?> entityCentre, final String icon, final PrefDim dim) {
        return new EntityActionConfig(ShowViewInDialogFunctionalEntity.class, entityCentre, context().withCurrentEntity().build(), icon, "Show detail view", null, null, null, null, dim, false);
    }

    /**
     * A factory method that creates a configuration for the required action.
     *
     * @param functionalEntity
     * @param context
     * @param icon
     * @param shortDesc
     * @param longDesc
     * @param preAction
     * @param successPostAction
     * @param errorPostAction
     * @return
     */
    public static EntityActionConfig createActionConfig(
            final Class<? extends AbstractFunctionalEntityWithCentreContext<?>> functionalEntity,
            final EntityCentre<?> entityCentre,
            final CentreContextConfig context,
            final String icon,
            final String shortDesc,
            final String longDesc,
            final IPreAction preAction,
            final IPostAction successPostAction,
            final IPostAction errorPostAction,
            final PrefDim prefDimForView
            ) {
        return new EntityActionConfig(
                functionalEntity,
                entityCentre,
                context,
                icon,
                shortDesc,
                longDesc,
                preAction,
                successPostAction,
                errorPostAction,
                prefDimForView,
                false);
    }

    /**
     * Indicates whether the action configuration should be used for instantiation of actions or to skip action creation.
     *
     * @return
     */
    public boolean isNoAction() {
        return noAction;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((context == null) ? 0 : context.hashCode());
        result = prime * result + ((errorPostAction == null) ? 0 : errorPostAction.hashCode());
        result = prime * result + ((functionalEntity == null) ? 0 : functionalEntity.hashCode());
        result = prime * result + ((icon == null) ? 0 : icon.hashCode());
        result = prime * result + ((longDesc == null) ? 0 : longDesc.hashCode());
        result = prime * result + (noAction ? 1231 : 1237);
        result = prime * result + ((preAction == null) ? 0 : preAction.hashCode());
        result = prime * result + ((shortDesc == null) ? 0 : shortDesc.hashCode());
        result = prime * result + ((successPostAction == null) ? 0 : successPostAction.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EntityActionConfig)) {
            return false;
        }

        final EntityActionConfig other = (EntityActionConfig) obj;
        if (context == null) {
            if (other.context != null) {
                return false;
            }
        } else if (!context.equals(other.context)) {
            return false;
        }
        if (errorPostAction == null) {
            if (other.errorPostAction != null) {
                return false;
            }
        } else if (!errorPostAction.equals(other.errorPostAction)) {
            return false;
        }
        if (functionalEntity == null) {
            if (other.functionalEntity != null) {
                return false;
            }
        } else if (!functionalEntity.equals(other.functionalEntity)) {
            return false;
        }
        if (icon == null) {
            if (other.icon != null) {
                return false;
            }
        } else if (!icon.equals(other.icon)) {
            return false;
        }
        if (longDesc == null) {
            if (other.longDesc != null) {
                return false;
            }
        } else if (!longDesc.equals(other.longDesc)) {
            return false;
        }
        if (noAction != other.noAction) {
            return false;
        }
        if (preAction == null) {
            if (other.preAction != null) {
                return false;
            }
        } else if (!preAction.equals(other.preAction)) {
            return false;
        }
        if (shortDesc == null) {
            if (other.shortDesc != null) {
                return false;
            }
        } else if (!shortDesc.equals(other.shortDesc)) {
            return false;
        }
        if (successPostAction == null) {
            if (other.successPostAction != null) {
                return false;
            }
        } else if (!successPostAction.equals(other.successPostAction)) {
            return false;
        }
        return true;
    }
}
