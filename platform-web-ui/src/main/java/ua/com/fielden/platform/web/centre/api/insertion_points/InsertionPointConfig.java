package ua.com.fielden.platform.web.centre.api.insertion_points;

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
    private boolean flex = false;
    private Optional<IToolbarConfig> toolbar = Optional.empty();

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

    public InsertionPointConfig setFlex(final boolean flex) {
        this.flex = flex;
        return this;
    }

    public boolean isFlex() {
        return flex;
    }

    public InsertionPointConfig setToolbar(final Optional<IToolbarConfig> toolbar) {
        this.toolbar = toolbar;
        return this;
    }

    public Optional<IToolbarConfig> getToolbar() {
        return toolbar;
    }
}
