package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@KeyType(String.class)
public class ReferenceHierarchyEntry extends AbstractTreeEntry<String> {

    @IsProperty
    @Title(value = "Reference Hierarchy Level", desc = "One of two available reference hierarchy levels: TYPE or INSTANCE")
    private String level;

    @Observable
    public ReferenceHierarchyEntry setLevel(final String level) {
        this.level = level;
        return this;
    }

    public String getLevel() {
        return level;
    }

    public ReferenceHierarchyEntry setHierarchyLevel(final ReferenceHierarchyLevel level) {
        setLevel(level.name());
        return this;
    }

    public ReferenceHierarchyLevel getHierarchyLevel() {
        return ReferenceHierarchyLevel.valueOf(this.level);
    }

}
