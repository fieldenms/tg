package ua.com.fielden.platform.web.centre.api.actions;

import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.web.PrefDim;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.insertion_points.InsertionPoints;
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
    public final Optional<CentreContextConfig> context;
    public final Optional<String> icon;
    public final Optional<String> shortDesc;
    public final Optional<String> longDesc;
    public final Optional<String> shortcut;
    public final Optional<IPreAction> preAction;
    public final Optional<IPostAction> successPostAction;
    public final Optional<IPostAction> errorPostAction;
    public final Optional<PrefDim> prefDimForView;
    private final boolean noAction;
    public final Optional<InsertionPoints> whereToInsertView;
	public final boolean shouldRefreshParentCentreAfterSave;
	public final UI_ROLE role;

	public enum UI_ROLE {
	    ICON, BUTTON;
	}

    private EntityActionConfig(
            final Class<? extends AbstractFunctionalEntityWithCentreContext<?>> functionalEntity,
            final CentreContextConfig context,
            final String icon,
            final String shortDesc,
            final String longDesc,
            final String shortcut,
            final IPreAction preAction,
            final IPostAction successPostAction,
            final IPostAction errorPostAction,
            final PrefDim prefDimForView,
            final boolean noAction,
            final boolean shouldRefreshParentCentreAfterSave,
            final InsertionPoints whereToInsertView,
            final UI_ROLE role) {

        if (!noAction && functionalEntity == null) {
            throw new IllegalArgumentException("A functional entity type should be provided.");
        }

        if (functionalEntity != null && context == null) {
            throw new IllegalArgumentException("Any functional entity requires some execution context to be specified.");
        }

        this.shouldRefreshParentCentreAfterSave = shouldRefreshParentCentreAfterSave;
        this.functionalEntity = Optional.ofNullable(functionalEntity);
        this.context = Optional.ofNullable(context);
        this.icon = Optional.ofNullable(icon);
        this.shortDesc = Optional.ofNullable(shortDesc);
        //Setting the long desc. If it's null then long desc should be equal to functional entity description.
        String enhancedLongDesc = longDesc;
        if (enhancedLongDesc == null && functionalEntity != null) {
            enhancedLongDesc = TitlesDescsGetter.getEntityTitleAndDesc(functionalEntity).getValue();
        }
        this.longDesc = Optional.ofNullable(enhancedLongDesc);
        this.shortcut = Optional.ofNullable(shortcut);
        this.preAction = Optional.ofNullable(preAction);
        this.successPostAction = Optional.ofNullable(successPostAction);
        this.errorPostAction = Optional.ofNullable(errorPostAction);
        this.prefDimForView = Optional.ofNullable(prefDimForView);
        this.noAction = noAction;
        this.whereToInsertView = Optional.ofNullable(whereToInsertView);
        this.role = role;
    }


    private EntityActionConfig(
            final Class<? extends AbstractFunctionalEntityWithCentreContext<?>> functionalEntity,
            final CentreContextConfig context,
            final String icon,
            final String shortDesc,
            final String longDesc,
            final String shortcut,
            final IPreAction preAction,
            final IPostAction successPostAction,
            final IPostAction errorPostAction,
            final PrefDim prefDimForView,
            final boolean noAction,
            final boolean shouldRefreshParentCentreAfterSave) {
        this(functionalEntity, context, icon, shortDesc, longDesc, shortcut, preAction, successPostAction, errorPostAction, prefDimForView, noAction, shouldRefreshParentCentreAfterSave, null, UI_ROLE.ICON);
    }


    /**
     * Makes a new configuration based on the passed in configuration to become as associated with the specified insertion point.
     */
    public static EntityActionConfig mkInsertionPoint(final EntityActionConfig ac, final InsertionPoints ip) {
        return new EntityActionConfig(
                ac.functionalEntity.isPresent() ? ac.functionalEntity.get() : null,
                ac.context.isPresent() ? ac.context.get() : null,
                ac.icon.isPresent() ? ac.icon.get() : null,
                ac.shortDesc.isPresent() ? ac.shortDesc.get() : null,
                ac.longDesc.isPresent() ? ac.longDesc.get() : null,
                ac.shortcut.isPresent() ? ac.shortcut.get() : null,
                ac.preAction.isPresent() ? ac.preAction.get() : null,
                ac.successPostAction.isPresent() ? ac.successPostAction.get() : null,
                ac.errorPostAction.isPresent() ? ac.errorPostAction.get() : null,
                ac.prefDimForView.isPresent() ? ac.prefDimForView.get() : null,
                ac.noAction,
                ac.shouldRefreshParentCentreAfterSave,
                ip,
                UI_ROLE.ICON);
    }

    /**
     * Makes a new configuration based on the current one, but with the specified role.
     *
     * @param ac
     * @param role
     * @return
     */
    public static EntityActionConfig setRole(final EntityActionConfig ac, final UI_ROLE role) {
        return new EntityActionConfig(
                ac.functionalEntity.isPresent() ? ac.functionalEntity.get() : null,
                ac.context.isPresent() ? ac.context.get() : null,
                ac.icon.isPresent() ? ac.icon.get() : null,
                ac.shortDesc.isPresent() ? ac.shortDesc.get() : null,
                ac.longDesc.isPresent() ? ac.longDesc.get() : null,
                ac.shortcut.isPresent() ? ac.shortcut.get() : null,
                ac.preAction.isPresent() ? ac.preAction.get() : null,
                ac.successPostAction.isPresent() ? ac.successPostAction.get() : null,
                ac.errorPostAction.isPresent() ? ac.errorPostAction.get() : null,
                ac.prefDimForView.isPresent() ? ac.prefDimForView.get() : null,
                ac.noAction,
                ac.shouldRefreshParentCentreAfterSave,
                ac.whereToInsertView.isPresent() ? ac.whereToInsertView.get() : null,
                role);
    }

    /**
     * A factory method for creating a configuration that indicates a need to skip creation of any action.
     *
     * @return
     */
    public static EntityActionConfig createNoActionConfig() {
        return new EntityActionConfig(null, null, null, null, null, null, null, null, null, null, true, true);
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
            final String shortcut,
            final IPreAction preAction,
            final IPostAction successPostAction,
            final IPostAction errorPostAction,
            final PrefDim prefDimForView,
            final boolean shouldRefreshParentCentreAfterSave
            ) {
        return new EntityActionConfig(
                functionalEntity,
                context,
                icon,
                shortDesc,
                longDesc,
                shortcut,
                preAction,
                successPostAction,
                errorPostAction,
                prefDimForView,
                false,
                shouldRefreshParentCentreAfterSave);
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
        result = prime * result + ((shortcut == null) ? 0 : shortcut.hashCode());
        result = prime * result + (noAction ? 1231 : 1237);
        result = prime * result + ((preAction == null) ? 0 : preAction.hashCode());
        result = prime * result + ((shortDesc == null) ? 0 : shortDesc.hashCode());
        result = prime * result + ((successPostAction == null) ? 0 : successPostAction.hashCode());
        result = prime * result + ((whereToInsertView == null) ? 0 : whereToInsertView.hashCode());
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
        if (shortcut == null) {
            if (other.shortcut != null) {
                return false;
            }
        } else if (!shortcut.equals(other.shortcut)) {
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
        if (whereToInsertView == null) {
            if (other.whereToInsertView != null) {
                return false;
            }
        } else if (!whereToInsertView.equals(other.whereToInsertView)) {
            return false;
        }
        return true;
    }
}
