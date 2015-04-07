package ua.com.fielden.platform.web.centre.api.actions;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
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
    public final Class<? extends AbstractFunctionalEntityWithCentreContext<?>> functionalEntity;
    public final CentreContextConfig context;
    public final String icon;
    public final String shortDesc;
    public final String longDesc;
    public final IPreAction preAciton;
    public final IPostAction successPostAction;
    public final IPostAction errorPostAction;
    private final boolean noAction;

    private EntityActionConfig(
            final Class<? extends AbstractFunctionalEntityWithCentreContext<?>> functionalEntity,
            final CentreContextConfig context,
            final String icon,
            final String shortDesc,
            final String longDesc,
            final IPreAction preAction,
            final IPostAction successPostAction,
            final IPostAction errorPostAction,
            final boolean noAction
            ) {
        this.functionalEntity = functionalEntity;
        this.context = context;
        this.icon = icon;
        this.shortDesc = shortDesc;
        this.longDesc = longDesc;
        this.preAciton = preAction;
        this.successPostAction = successPostAction;
        this.errorPostAction = errorPostAction;
        this.noAction = noAction;
    }

    /**
     * A factory method for creating a configuration that indicates a need to skip creation of any action.
     * @return
     */
    public static EntityActionConfig createNoActionConfig() {
        return new EntityActionConfig(null, null, null, null, null, null, null, null, true);
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
                    final CentreContextConfig context,
                    final String icon,
                    final String shortDesc,
                    final String longDesc,
                    final IPreAction preAction,
                    final IPostAction successPostAction,
                    final IPostAction errorPostAction
            ) {
        return new EntityActionConfig(
                functionalEntity,
                context,
                icon,
                shortDesc,
                longDesc,
                preAction,
                successPostAction,
                errorPostAction,
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
        result = prime * result + ((preAciton == null) ? 0 : preAciton.hashCode());
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
        if (preAciton == null) {
            if (other.preAciton != null) {
                return false;
            }
        } else if (!preAciton.equals(other.preAciton)) {
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
