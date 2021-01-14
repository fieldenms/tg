package ua.com.fielden.platform.web.centre.api.actions.multi;

/**
 * A contract that builds multi-action configuration object.
 *
 * @author TG Team
 *
 */
public interface IEntityMultiActionConfigBuild extends IEntityMultiActionConfigAddAction {

    /**
     * Builds multi-action configuration object.
     *
     * @return
     */
    EntityMultiActionConfig build();
}
