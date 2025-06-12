package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

///
/// Functional entity for an entity master with an entity centre that can be changed by server side logic.
///
@EntityTitle("Compound Menu Item for Volatile Entity Centre")
public abstract class AbstractVolatileCentreCompoundMenuItem<T extends AbstractEntity<?>> extends AbstractFunctionalEntityForCompoundMenuItem<T> {

    @IsProperty
    @Title(value = "Menu Item Type", desc = "Entity Centre Menu Item Type")
    private String menuItemType;

    @IsProperty
    @Title(value = "Should Force Post Save Refresh?", desc="Indicates whether the centre should be refreshed after a successful save of the entity master")
    private boolean shouldEnforcePostSaveRefresh;

    @IsProperty
    @Title(value = "Event Source Class", desc = "Event Source Class Name")
    private String eventSourceClass;

    public String getEventSourceClass() {
        return eventSourceClass;
    }

    @Observable
    public AbstractVolatileCentreCompoundMenuItem setEventSourceClass(final String eventSourceClass) {
        this.eventSourceClass = eventSourceClass;
        return this;
    }
    public boolean getShouldEnforcePostSaveRefresh() {
        return shouldEnforcePostSaveRefresh;
    }

    @Observable
    public AbstractVolatileCentreCompoundMenuItem setShouldEnforcePostSaveRefresh(final boolean shouldEnforcePostSaveRefresh) {
        this.shouldEnforcePostSaveRefresh = shouldEnforcePostSaveRefresh;
        return this;
    }
    public String getMenuItemType() {
        return menuItemType;
    }

    @Observable
    public AbstractVolatileCentreCompoundMenuItem setMenuItemType(final String menuItemType) {
        this.menuItemType = menuItemType;
        return this;
    }
}
