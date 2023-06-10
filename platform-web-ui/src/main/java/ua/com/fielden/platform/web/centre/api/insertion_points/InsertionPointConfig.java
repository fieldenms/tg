package ua.com.fielden.platform.web.centre.api.insertion_points;

import static java.util.Optional.empty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.resultset.toolbar.IToolbarConfig;

/**
 * Represents the insertion point configuration. Configuration includes the entity action configuration object and indicator whether insertion point
 * has pagination buttons or not.
 *
 * @author TG Team
 *
 */
public class InsertionPointConfig {

    private final EntityActionConfig insertionPointAction;
    private boolean preferred = false;
    private boolean noResizing = false;
    private final List<EntityActionConfig> actions = new ArrayList<>();
    private Optional<IToolbarConfig> toolbar = empty();

    /**
     * Initiates the insertion point configuration object with action configuration and pagination indicator
     *
     * @param insertionPointAction
     */
    private InsertionPointConfig(final EntityActionConfig insertionPointAction) {
        this.insertionPointAction = insertionPointAction;
    }

    /**
     * A factory method for creating insertion point configuration without pagination controls.
     *
     * @param insertionPointAction
     * @return
     */
    public static InsertionPointConfig configInsertionPoint(final EntityActionConfig insertionPointAction) {
        return new InsertionPointConfig(insertionPointAction);
    }

    public EntityActionConfig getInsertionPointAction() {
        return insertionPointAction;
    }

    public InsertionPointConfig setPreferred(final boolean preferred) {
        this.preferred = preferred;
        return this;
    }

    public boolean isPreferred() {
        return preferred;
    }

    public InsertionPointConfig setNoResizing(final boolean noResizing) {
        this.noResizing = noResizing;
        return this;
    }

    public boolean isNoResizing() {
        return noResizing;
    }

    public InsertionPointConfig setToolbar(final Optional<IToolbarConfig> toolbar) {
        this.toolbar = toolbar;
        return this;
    }

    public Optional<IToolbarConfig> getToolbar() {
        return toolbar;
    }

    public InsertionPointConfig setActions(final List<EntityActionConfig> actions) {
        this.actions.clear();
        this.actions.addAll(actions);
        return this;
    }

    public List<EntityActionConfig> getActions() {
        return Collections.unmodifiableList(actions);
    }
}
