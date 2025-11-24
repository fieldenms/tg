package ua.com.fielden.platform.web.centre.api.actions;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.web.PrefDim;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.insertion_points.InsertionPoints;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/// Represents the configuration of an action in the Web UI configuration.
///
/// Multiple configurations may exist for any given action entity (e.g., one for the standalone centre, another one for an embedded centre).
///
public final class EntityActionConfig {

    public final Optional<Class<? extends AbstractFunctionalEntityWithCentreContext<?>>> functionalEntity;
    public final Optional<String> actionIdentifier;
    public final Optional<CentreContextConfig> context;
    public final Optional<String> icon;
    public final Optional<String> iconStyle;
    public final Optional<String> shortDesc;
    public final Optional<String> longDesc;
    public final Optional<String> shortcut;
    public final Optional<IPreAction> preAction;
    public final Optional<IPostAction> successPostAction;
    public final Optional<IPostAction> errorPostAction;
    public final Optional<PrefDim> prefDimForView;
    public final Optional<InsertionPoints> whereToInsertView;
	public final boolean shouldRefreshParentCentreAfterSave;
	public final Set<Class<? extends AbstractFunctionalEntityWithCentreContext<?>>> excludeInsertionPoints = new HashSet<>();
	public final UI_ROLE role;

    public enum UI_ROLE {
	    ICON, BUTTON;
	}

    private EntityActionConfig(
            final Class<? extends AbstractFunctionalEntityWithCentreContext<?>> functionalEntity,
            final CharSequence actionIdentifier,
            final CentreContextConfig context,
            final String icon,
            final String iconStyle,
            final String shortDesc,
            final String longDesc,
            final String shortcut,
            final IPreAction preAction,
            final IPostAction successPostAction,
            final IPostAction errorPostAction,
            final PrefDim prefDimForView,
            final boolean shouldRefreshParentCentreAfterSave,
            final InsertionPoints whereToInsertView,
            final UI_ROLE role,
            final Set<Class<? extends AbstractFunctionalEntityWithCentreContext<?>>> excludeInsertionPoints)
    {
        if (context == null) {
            throw new IllegalArgumentException("Any functional entity requires some execution context to be specified.");
        }

        if (functionalEntity == null && !context.withCurrentEtity) {
            throw new IllegalArgumentException("Dynamic action can be created only with current entity in context.");
        }

        this.actionIdentifier = Optional.ofNullable(actionIdentifier).map(CharSequence::toString);
        this.shouldRefreshParentCentreAfterSave = shouldRefreshParentCentreAfterSave;
        this.functionalEntity = Optional.ofNullable(functionalEntity);
        this.context = Optional.ofNullable(context);
        this.icon = Optional.ofNullable(icon);
        this.iconStyle = Optional.ofNullable(iconStyle);
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
        this.whereToInsertView = Optional.ofNullable(whereToInsertView);
        this.role = role;
        this.excludeInsertionPoints.addAll(excludeInsertionPoints);
    }

    private EntityActionConfig(
            final Class<? extends AbstractFunctionalEntityWithCentreContext<?>> functionalEntity,
            final CharSequence actionIdentifier,
            final CentreContextConfig context,
            final String icon,
            final String iconStyle,
            final String shortDesc,
            final String longDesc,
            final String shortcut,
            final IPreAction preAction,
            final IPostAction successPostAction,
            final IPostAction errorPostAction,
            final PrefDim prefDimForView,
            final boolean shouldRefreshParentCentreAfterSave,
            final Set<Class<? extends AbstractFunctionalEntityWithCentreContext<?>>> excludeInsertionPoints)
    {
        this(functionalEntity,
             actionIdentifier,
             context,
             icon,
             iconStyle,
             shortDesc,
             longDesc,
             shortcut,
             preAction,
             successPostAction,
             errorPostAction,
             prefDimForView,
             shouldRefreshParentCentreAfterSave,
             null,
             UI_ROLE.ICON,
             excludeInsertionPoints);
    }

    /// Creates a new configuration based on `ac` but with the specified centre context.
    ///
    public static EntityActionConfig withContext(final EntityActionConfig ac, final CentreContextConfig cc) {
        return new EntityActionConfig(
                ac.functionalEntity.orElse(null),
                ac.actionIdentifier.orElse(null),
                cc,
                ac.icon.orElse(null),
                ac.iconStyle.orElse(null),
                ac.shortDesc.orElse(null),
                ac.longDesc.orElse(null),
                ac.shortcut.orElse(null),
                ac.preAction.orElse(null),
                ac.successPostAction.orElse(null),
                ac.errorPostAction.orElse(null),
                ac.prefDimForView.orElse(null),
                ac.shouldRefreshParentCentreAfterSave,
                ac.whereToInsertView.orElse(null),
                ac.role,
                ac.excludeInsertionPoints);
    }


    /// Creates a new configuration based on `ac` but with the specified insertion point.
    ///
    public static EntityActionConfig mkInsertionPoint(final EntityActionConfig ac, final InsertionPoints ip) {
        return new EntityActionConfig(
                ac.functionalEntity.orElse(null),
                ac.actionIdentifier.orElse(null),
                ac.context.orElse(null),
                ac.icon.orElse(null),
                ac.iconStyle.orElse(null),
                ac.shortDesc.orElse(null),
                ac.longDesc.orElse(null),
                ac.shortcut.orElse(null),
                ac.preAction.orElse(null),
                ac.successPostAction.orElse(null),
                ac.errorPostAction.orElse(null),
                ac.prefDimForView.orElse(null),
                ac.shouldRefreshParentCentreAfterSave,
                ip,
                UI_ROLE.ICON,
                ac.excludeInsertionPoints);
    }

    /// Creates a new configuration based on the current one, but with the specified role.
    ///
    public static EntityActionConfig setRole(final EntityActionConfig ac, final UI_ROLE role) {
        return new EntityActionConfig(
                ac.functionalEntity.orElse(null),
                ac.actionIdentifier.orElse(null),
                ac.context.orElse(null),
                ac.icon.orElse(null),
                ac.iconStyle.orElse(null),
                ac.shortDesc.orElse(null),
                ac.longDesc.orElse(null),
                ac.shortcut.orElse(null),
                ac.preAction.orElse(null),
                ac.successPostAction.orElse(null),
                ac.errorPostAction.orElse(null),
                ac.prefDimForView.orElse(null),
                ac.shouldRefreshParentCentreAfterSave,
                ac.whereToInsertView.orElse(null),
                role,
                ac.excludeInsertionPoints);
    }

    public static EntityActionConfig createActionConfig(
            final Class<? extends AbstractFunctionalEntityWithCentreContext<?>> functionalEntity,
            final CharSequence actionIdentifier,
            final CentreContextConfig context,
            final String icon,
            final String iconStyle,
            final String shortDesc,
            final String longDesc,
            final String shortcut,
            final IPreAction preAction,
            final IPostAction successPostAction,
            final IPostAction errorPostAction,
            final PrefDim prefDimForView,
            final boolean shouldRefreshParentCentreAfterSave,
            final Set<Class<? extends AbstractFunctionalEntityWithCentreContext<?>>> excludeInsertionPoints
            ) {
        return new EntityActionConfig(
                functionalEntity,
                actionIdentifier,
                context,
                icon,
                iconStyle,
                shortDesc,
                longDesc,
                shortcut,
                preAction,
                successPostAction,
                errorPostAction,
                prefDimForView,
                shouldRefreshParentCentreAfterSave,
                excludeInsertionPoints);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + context.hashCode();
        result = prime * result + actionIdentifier.hashCode();
        result = prime * result + errorPostAction.hashCode();
        result = prime * result + functionalEntity.hashCode();
        result = prime * result + icon.hashCode();
        result = prime * result + longDesc.hashCode();
        result = prime * result + shortcut.hashCode();
        result = prime * result + preAction.hashCode();
        result = prime * result + shortDesc.hashCode();
        result = prime * result + successPostAction.hashCode();
        result = prime * result + whereToInsertView.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
                || obj instanceof EntityActionConfig that
                   && Objects.equals(context, that.context)
                   && Objects.equals(errorPostAction, that.errorPostAction)
                   && Objects.equals(functionalEntity, that.functionalEntity)
                   && Objects.equals(actionIdentifier, that.actionIdentifier)
                   && Objects.equals(icon, that.icon)
                   && Objects.equals(longDesc, that.longDesc)
                   && Objects.equals(shortcut, that.shortcut)
                   && Objects.equals(preAction, that.preAction)
                   && Objects.equals(shortDesc, that.shortDesc)
                   && Objects.equals(successPostAction, that.successPostAction)
                   && Objects.equals(whereToInsertView, that.whereToInsertView);
    }

}
