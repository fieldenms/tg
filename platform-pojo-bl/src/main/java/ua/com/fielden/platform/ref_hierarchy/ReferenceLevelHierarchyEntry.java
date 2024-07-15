package ua.com.fielden.platform.ref_hierarchy;

import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Represents the pair between property and entity that was referenced by examining entity in reference hierarchy.
 *
 * @author TG Team
 *
 */
public class ReferenceLevelHierarchyEntry extends ReferencedByLevelHierarchyEntry {

    @IsProperty
    @Title("Property title")
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