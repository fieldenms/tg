package ua.com.fielden.platform.web.centre.api.actions.multi;

import static java.util.Optional.of;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;

/**
 * Implements the multi-action construction API.
 *
 * @author TG Team
 *
 */
public class EntityMultiActionConfigBuilder implements IEntityMultiActionConfigAddAction, IEntityMultiActionConfigBuild {
    private final Class<? extends IEntityMultiActionSelector> actionSelectorClass;
    private final List<Supplier<Optional<EntityActionConfig>>> actions = new ArrayList<>();

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
        if (actions.isEmpty()) {
            throw new IllegalStateException("Multi-action configuration is empty."); // API does not allow this, but check emptiness for additional safety.
        }
        return new EntityMultiActionConfig(actionSelectorClass, actions);
    }

    @Override
    public IEntityMultiActionConfigBuild addAction(final EntityActionConfig action) {
        actions.add(() -> of(action));
        return this;
    }

    @Override
    public IEntityMultiActionConfigBuild addAction(final Supplier<Optional<EntityActionConfig>> action) {
        actions.add(action);
        return this;
    }

}