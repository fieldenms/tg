package ua.com.fielden.platform.web.centre.api.actions.multi;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;

/**
 * Implements the multi-action construction API.
 *
 * @author TG Team
 *
 */
public class EntityMultiActionConfigBuilder implements IEntityMultiActionConfigAddAction, IEntityMultiActionConfigBuild {

    private final Class<? extends IEntityMultiActionSelector> actionSelectorClass;
    private final List<EntityActionConfig> actions = new ArrayList<>();

    /**
     * Creates the multi-action configuration with entry point that allows one to add first action configuration.
     *
     * @param actionSelectorClass
     * @return
     */
    public static IEntityMultiActionConfigAddAction multiAction(final Class<? extends IEntityMultiActionSelector> actionSelectorClass) {
        return new EntityMultiActionConfigBuilder(actionSelectorClass);
    }

    /**
     * Creates the multi-action configuration builder for specified action selector class.
     *
     * @param actionSelectorClass
     */
    private EntityMultiActionConfigBuilder(final Class<? extends IEntityMultiActionSelector> actionSelectorClass) {
        this.actionSelectorClass = actionSelectorClass;
    }

    @Override
    public EntityMultiActionConfig build() {
        return new EntityMultiActionConfig(actionSelectorClass, actions);
    }

    @Override
    public IEntityMultiActionConfigBuild addAction(final EntityActionConfig action) {
        actions.add(action);
        return this;
    }

}
