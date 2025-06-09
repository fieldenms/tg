package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.*;

///
/// Functional entity that represents an entity master with a generic audit entity centre.
///
@EntityTitle("Menu Item for Audit Entity Centre")
@CompanionObject(FunctionalEntityForAuditCentreCompoundMenuItemCo.class)
public class FunctionalEntityForAuditCentreCompoundMenuItem extends AbstractFunctionalEntityForCompoundMenuItem<AbstractEntity<?>> {

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
    public FunctionalEntityForAuditCentreCompoundMenuItem setEventSourceClass(final String eventSourceClass) {
        this.eventSourceClass = eventSourceClass;
        return this;
    }
    public boolean getShouldEnforcePostSaveRefresh() {
        return shouldEnforcePostSaveRefresh;
    }

    @Observable
    public FunctionalEntityForAuditCentreCompoundMenuItem setShouldEnforcePostSaveRefresh(final boolean shouldEnforcePostSaveRefresh) {
        this.shouldEnforcePostSaveRefresh = shouldEnforcePostSaveRefresh;
        return this;
    }
    public String getMenuItemType() {
        return menuItemType;
    }

    @Observable
    public FunctionalEntityForAuditCentreCompoundMenuItem setMenuItemType(final String menuItemType) {
        this.menuItemType = menuItemType;
        return this;
    }
}
