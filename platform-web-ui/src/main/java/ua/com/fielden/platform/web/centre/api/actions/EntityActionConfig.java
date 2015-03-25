package ua.com.fielden.platform.web.centre.api.actions;

import ua.com.fielden.platform.entity.AbstractEntity;
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
    public final Class<? extends AbstractEntity<?>> functionalEntity;
    public final CentreContextConfig context;
    public final String icon;
    public final String shortDesc;
    public final String longDesc;
    public final IPreAction preAciton;
    public final IPostAction successPostAction;
    public final IPostAction errorPostAction;

    public EntityActionConfig(
            final Class<? extends AbstractEntity<?>> functionalEntity,
            final CentreContextConfig context,
            final String icon,
            final String shortDesc,
            final String longDesc,
            final IPreAction preAction,
            final IPostAction successPostAction,
            final IPostAction errorPostAction
            ) {
        this.functionalEntity = functionalEntity;
        this.context = context;
        this.icon = icon;
        this.shortDesc = shortDesc;
        this.longDesc = longDesc;
        this.preAciton = preAction;
        this.successPostAction = successPostAction;
        this.errorPostAction = errorPostAction;
    }

}
