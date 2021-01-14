package ua.com.fielden.platform.web.centre.api.actions.multi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;

/**
 * Multi-action configuration object.
 *
 * @author TG Team
 *
 */
public class EntityMultiActionConfig {

    private final Class<? extends IEntityMultiActionSelector> actionSelectorClass;
    private final List<EntityActionConfig> actions = new ArrayList<>();

    public EntityMultiActionConfig (final Class<? extends IEntityMultiActionSelector> actionSelectorClass, final List<EntityActionConfig> actions) {
        this.actionSelectorClass = actionSelectorClass;
        this.actions.addAll(actions);
    }

    /**
     * Returns the action selector class.
     *
     * @return
     */
    public Class<? extends IEntityMultiActionSelector> actionSelectorClass() {
        return actionSelectorClass;
    }

    /**
     * Returns the list of actions from which each action will be selected for specific entity.
     *
     * @return
     */
    public List<EntityActionConfig> actions() {
        return Collections.unmodifiableList(actions);
    }

}