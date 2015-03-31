package ua.com.fielden.platform.web.centre.api.actions;

import ua.com.fielden.platform.entity.AbstractEntity;
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
public class EntityActionConfig {
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

}
