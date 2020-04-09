package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@KeyTitle("Entity key")
@DescTitle("Entity Description")
public class InstanceLevelHierarchyEntry extends ReferenceHierarchyEntry{

    @IsProperty
    @Title(value = "Key object", desc = "Desc")
    private Object entityKey;

    @Observable
    public InstanceLevelHierarchyEntry setEntityKey(final Object entityKey) {
        this.entityKey = entityKey;
        return this;
    }

    public Object getEntityKey() {
        return entityKey;
    }

    @IsProperty
    @Title("Has Dependencies?")
    private boolean hasDependencies;

    @Observable
    public InstanceLevelHierarchyEntry setHasDependencies(final boolean hasDependencies) {
        this.hasDependencies = hasDependencies;
        return this;
    }

    public boolean getHasDependencies() {
        return hasDependencies;
    }

}
