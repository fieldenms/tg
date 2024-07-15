package ua.com.fielden.platform.web.centre.api.actions.multi;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;

/**
 * Multi-action configuration object.
 *
 * @author TG Team
 *
 */
public class EntityMultiActionConfig {
    private final Class<? extends IEntityMultiActionSelector> actionSelectorClass;
    private final List<Supplier<Optional<EntityActionConfig>>> actions = new ArrayList<>();

    /**
     * Creates multi-action configuration object from action selector class and list of action configurations from which the active action will be selected.
     */
    public EntityMultiActionConfig(final Class<? extends IEntityMultiActionSelector> actionSelectorClass, final List<Supplier<Optional<EntityActionConfig>>> actions) {
        this.actionSelectorClass = actionSelectorClass;
        this.actions.addAll(actions);
    }

    /**
     * Returns the action selector class.
     */
    public Class<? extends IEntityMultiActionSelector> actionSelectorClass() {
        return actionSelectorClass;
    }

    /**
     * Returns the list of action configurations from which the active action will be selected for specific entity.
     *
     * @return
     */
    public List<EntityActionConfig> actions() {
        return unmodifiableList(actions.stream().map(sup -> sup.get()).filter(Optional::isPresent).map(Optional::get).collect(toList()));
    }

}