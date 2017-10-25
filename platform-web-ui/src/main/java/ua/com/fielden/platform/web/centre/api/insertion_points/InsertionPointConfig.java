package ua.com.fielden.platform.web.centre.api.insertion_points;

import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;

public class InsertionPointConfig {

    private final EntityActionConfig insertionPointAction;
    private final boolean hasPaginationButtons;
    
    public InsertionPointConfig(final EntityActionConfig insertionPointAction, final boolean hasPaginationButtons) {
        this.insertionPointAction = insertionPointAction;
        this.hasPaginationButtons = hasPaginationButtons;
    }

    public InsertionPointConfig(final EntityActionConfig insertionPointAction) {
        this(insertionPointAction, false);
    }

    public EntityActionConfig getInsertionPointAction() {
        return insertionPointAction;
    }

    public boolean hasPaginationButtons() {
        return hasPaginationButtons;
    }
}
