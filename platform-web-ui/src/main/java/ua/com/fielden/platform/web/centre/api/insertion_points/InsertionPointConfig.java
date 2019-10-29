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
    private InsertionPointConfig(final EntityActionConfig insertionPointAction, final boolean hasPaginationButtons) {
        this.insertionPointAction = insertionPointAction;
        this.hasPaginationButtons = hasPaginationButtons;
    }

    /**
     * A factory method for creating insertion point configuration without pagination controls. 
     * 
     * @param insertionPointAction
     * @return
     */
    public static InsertionPointConfig configInsertionPoint(final EntityActionConfig insertionPointAction) {
        return new InsertionPointConfig(insertionPointAction, false);
    }
    
    /**
     * A factory method for creating insertion point configuration with pagination controls.
     * 
     * @param insertionPointAction
     * @return
     */
    public static InsertionPointConfig configInsertionPointWithPagination(final EntityActionConfig insertionPointAction) {
        return new InsertionPointConfig(insertionPointAction, true);
    }
    
    public EntityActionConfig getInsertionPointAction() {
        return insertionPointAction;
    }

    public boolean hasPaginationButtons() {
        return hasPaginationButtons;
    }
}
