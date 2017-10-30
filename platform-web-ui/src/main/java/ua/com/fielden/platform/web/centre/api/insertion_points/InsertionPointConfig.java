package ua.com.fielden.platform.web.centre.api.insertion_points;

import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;

/**
 * Represents the insertion point configuration. Configuration includes the entity action configuration object and indicator whether insertion point
 * has pagination buttons or not.
 * 
 * @author TG Team
 *
 */
public class InsertionPointConfig {

    private final EntityActionConfig insertionPointAction;
    private final boolean hasPaginationButtons;
    
    /**
     * Initiates the insertion point configuration object with action configuration and pagination indicator 
     * 
     * @param insertionPointAction
     * @param hasPaginationButtons
     */
    public InsertionPointConfig(final EntityActionConfig insertionPointAction, final boolean hasPaginationButtons) {
        this.insertionPointAction = insertionPointAction;
        this.hasPaginationButtons = hasPaginationButtons;
    }

    public EntityActionConfig getInsertionPointAction() {
        return insertionPointAction;
    }

    public boolean hasPaginationButtons() {
        return hasPaginationButtons;
    }
}
