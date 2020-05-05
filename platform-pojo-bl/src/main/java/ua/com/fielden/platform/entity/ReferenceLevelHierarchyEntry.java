package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@CompanionObject(IReferenceLevelHierarchyEntry.class)
public class ReferenceLevelHierarchyEntry extends ReferencedByLevelHierarchyEntry {

    @IsProperty
    @Title(value = "Property title", desc = "Desc")
    private String propertyTitle;

    @Observable
    public ReferenceLevelHierarchyEntry setPropertyTitle(final String propertyTitle) {
        this.propertyTitle = propertyTitle;
        return this;
    }

    public String getPropertyTitle() {
        return propertyTitle;
    }



}
