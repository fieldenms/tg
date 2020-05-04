package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.master.MasterInfo;

@KeyTitle("Entity key")
@DescTitle("Entity Description")
public class ReferencedByLevelHierarchyEntry extends ReferenceHierarchyEntry {

    @IsProperty
    @Title(value = "Entity", desc = "Entity refrence")
    private AbstractEntity<?> entity;

    @IsProperty
    @Title(value = "Open Master Action", desc = "Open Action Master Configuration")
    private MasterInfo masterInfo;

    @Observable
    public ReferencedByLevelHierarchyEntry setMasterInfo(final MasterInfo masterInfo) {
        this.masterInfo = masterInfo;
        return this;
    }

    public MasterInfo getMasterInfo() {
        return masterInfo;
    }

    @Observable
    public ReferencedByLevelHierarchyEntry setEntity(final AbstractEntity<?> entity) {
        this.entity = entity;
        return this;
    }

    public AbstractEntity<?> getEntity() {
        return entity;
    }
}
