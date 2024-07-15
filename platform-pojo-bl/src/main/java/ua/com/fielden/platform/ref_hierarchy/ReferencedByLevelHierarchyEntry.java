package ua.com.fielden.platform.ref_hierarchy;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Represents the entity that was referenced by another examining entity in reference hierarchy.
 *
 * @author TG Team
 *
 */
@KeyTitle("Entity key")
@DescTitle("Entity Description")
public class ReferencedByLevelHierarchyEntry extends ReferenceHierarchyEntry {

    @IsProperty
    @Title(value = "Entity", desc = "Entity refrence")
    private AbstractEntity<?> entity;

    @Observable
    public ReferencedByLevelHierarchyEntry setEntity(final AbstractEntity<?> entity) {
        this.entity = entity;
        return this;
    }

    public AbstractEntity<?> getEntity() {
        return entity;
    }

}